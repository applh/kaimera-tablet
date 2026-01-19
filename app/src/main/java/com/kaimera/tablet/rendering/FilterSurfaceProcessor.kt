package com.kaimera.tablet.rendering

import android.graphics.SurfaceTexture
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.Surface
import androidx.camera.core.SurfaceOutput
import androidx.camera.core.SurfaceProcessor
import androidx.camera.core.SurfaceRequest
import java.util.concurrent.Executor

class FilterSurfaceProcessor : SurfaceProcessor, SurfaceTexture.OnFrameAvailableListener {
    private val TAG = "FilterSurfaceProcessor"
    
    private val glThread = HandlerThread("GLThread")
    private val glHandler: Handler
    private val glExecutor: Executor
    
    private var eglManager: OpenGlUtilities.EglManager? = null
    private var textureRenderer: TextureRenderer? = null
    
    private var inputSurfaceTexture: SurfaceTexture? = null
    private var inputSurface: Surface? = null
    private var inputTextureId = 0
    
    private val outputSurfaces = mutableMapOf<SurfaceOutput, Surface>()
    
    private var isReleased = false
    
    init {
        glThread.start()
        glHandler = Handler(glThread.looper)
        glExecutor = Executor { command -> glHandler.post(command) }
        
        glExecutor.execute {
            initGl()
        }
    }
    
    private var dummySurface: android.opengl.EGLSurface? = null

    private fun initGl() {
        Log.d(TAG, "initGl: Starting GL initialization")
        try {
            eglManager = OpenGlUtilities.EglManager()
            eglManager?.init()
            
            // Create a dummy pbuffer to make context current for initialization
            dummySurface = eglManager?.makeCurrentPbuffer(1, 1)
            
            textureRenderer = TextureRenderer()
            
            val textIds = IntArray(1)
            android.opengl.GLES20.glGenTextures(1, textIds, 0)
            inputTextureId = textIds[0]
            
            inputSurfaceTexture = SurfaceTexture(inputTextureId).apply {
                setDefaultBufferSize(640, 480) 
                setOnFrameAvailableListener(this@FilterSurfaceProcessor, glHandler)
            }
            inputSurface = Surface(inputSurfaceTexture)
            Log.d(TAG, "initGl: GL Initialized successfully. TexId=$inputTextureId")
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to init GL", e)
        }
    }

    override fun onInputSurface(request: SurfaceRequest) {
        glExecutor.execute {
            if (isReleased) {
                request.willNotProvideSurface()
                return@execute
            }
            
            inputSurfaceTexture?.setDefaultBufferSize(request.resolution.width, request.resolution.height)
            
            inputSurface?.let { surface ->
                request.provideSurface(surface, glExecutor) { }
            } ?: run {
                Log.e(TAG, "onInputSurface: inputSurface is null")
                request.willNotProvideSurface()
            }
        }
    }

    override fun onOutputSurface(output: SurfaceOutput) {
        glExecutor.execute {
            if (isReleased) {
                output.close()
                return@execute
            }
            
            val surface = output.getSurface(glExecutor) {
                glExecutor.execute {
                    outputSurfaces.remove(output)
                }
            }
            
            outputSurfaces[output] = surface
        }
    }

    override fun onFrameAvailable(surfaceTexture: SurfaceTexture?) {
        if (isReleased || eglManager == null) return
        
        surfaceTexture?.updateTexImage()
        val transformMatrix = FloatArray(16)
        surfaceTexture?.getTransformMatrix(transformMatrix)
        val timestampNs = surfaceTexture?.timestamp ?: 0L
        
        for ((_, surface) in outputSurfaces) {
            try {
                if (eglManager?.makeCurrent(surface) == true) {
                    val width = eglManager?.querySurface(surface, android.opengl.EGL14.EGL_WIDTH) ?: 0
                    val height = eglManager?.querySurface(surface, android.opengl.EGL14.EGL_HEIGHT) ?: 0
                    
                    android.opengl.GLES20.glViewport(0, 0, width, height)
                    
                    textureRenderer?.draw(inputTextureId, transformMatrix)
                    
                    if (timestampNs > 0) {
                        eglManager?.setPresentationTime(surface, timestampNs)
                    }
                    eglManager?.swapBuffers(surface)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error rendering to surface", e)
            }
        }
    }
    
    fun setFilter(filterType: TextureRenderer.FilterType) {
        Log.d(TAG, "setFilter: $filterType")
        glExecutor.execute {
             textureRenderer?.setFilter(filterType)
        }
    }

    fun release() {
        Log.d(TAG, "release: Releasing processor")
        glExecutor.execute {
            isReleased = true
            outputSurfaces.clear()
            
            textureRenderer?.release()
            inputSurface?.release()
            inputSurfaceTexture?.release()
            
            eglManager?.release()
            
            glThread.quitSafely()
            Log.d(TAG, "release: Cleanup complete")
        }
    }
    
    object OpenGlUtilities {
        class EglManager {
            private var eglDisplay = android.opengl.EGL14.EGL_NO_DISPLAY
            private var eglContext = android.opengl.EGL14.EGL_NO_CONTEXT
            private var eglConfig: android.opengl.EGLConfig? = null
            
            fun init() {
                eglDisplay = android.opengl.EGL14.eglGetDisplay(android.opengl.EGL14.EGL_DEFAULT_DISPLAY)
                if (eglDisplay == android.opengl.EGL14.EGL_NO_DISPLAY) throw RuntimeException("eglGetDisplay failed")
                
                val version = IntArray(2)
                if (!android.opengl.EGL14.eglInitialize(eglDisplay, version, 0, version, 1)) {
                    throw RuntimeException("eglInitialize failed")
                }
                
                val attribList = intArrayOf(
                    android.opengl.EGL14.EGL_RED_SIZE, 8,
                    android.opengl.EGL14.EGL_GREEN_SIZE, 8,
                    android.opengl.EGL14.EGL_BLUE_SIZE, 8,
                    android.opengl.EGL14.EGL_ALPHA_SIZE, 8,
                    android.opengl.EGL14.EGL_RENDERABLE_TYPE, android.opengl.EGL14.EGL_OPENGL_ES2_BIT,
                    0x3142, 1, // EGL_RECORDABLE_ANDROID
                    android.opengl.EGL14.EGL_NONE
                )
                
                val configs = arrayOfNulls<android.opengl.EGLConfig>(1)
                val numConfigs = IntArray(1)
                if (!android.opengl.EGL14.eglChooseConfig(eglDisplay, attribList, 0, configs, 0, configs.size, numConfigs, 0)) {
                    throw RuntimeException("eglChooseConfig failed")
                }
                eglConfig = configs[0]
                
                val contextAttribs = intArrayOf(
                    android.opengl.EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
                    android.opengl.EGL14.EGL_NONE
                )
                
                eglContext = android.opengl.EGL14.eglCreateContext(eglDisplay, eglConfig, android.opengl.EGL14.EGL_NO_CONTEXT, contextAttribs, 0)
                if (eglContext == android.opengl.EGL14.EGL_NO_CONTEXT) {
                    throw RuntimeException("eglCreateContext failed")
                }
            }
            
            private val surfaceMap = mutableMapOf<Surface, android.opengl.EGLSurface>()
            
            fun makeCurrent(surface: Surface): Boolean {
                var eglSurface = surfaceMap[surface]
                if (eglSurface == null) {
                    val surfaceAttribs = intArrayOf(android.opengl.EGL14.EGL_NONE)
                    eglSurface = android.opengl.EGL14.eglCreateWindowSurface(eglDisplay, eglConfig, surface, surfaceAttribs, 0)
                    if (eglSurface == null || eglSurface == android.opengl.EGL14.EGL_NO_SURFACE) {
                        return false
                    }
                    surfaceMap[surface] = eglSurface
                }
                
                return android.opengl.EGL14.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext)
            }
            
            fun makeCurrentPbuffer(width: Int, height: Int): android.opengl.EGLSurface? {
                 val surfaceAttribs = intArrayOf(
                    android.opengl.EGL14.EGL_WIDTH, width,
                    android.opengl.EGL14.EGL_HEIGHT, height,
                    android.opengl.EGL14.EGL_NONE
                )
                val eglSurface = android.opengl.EGL14.eglCreatePbufferSurface(eglDisplay, eglConfig, surfaceAttribs, 0)
                 if (eglSurface == null || eglSurface == android.opengl.EGL14.EGL_NO_SURFACE) {
                        return null
                }
                if (!android.opengl.EGL14.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext)) {
                    return null
                }
                return eglSurface
            }

            fun setPresentationTime(surface: Surface, nsecs: Long): Boolean {
                val eglSurface = surfaceMap[surface] ?: return false
                return android.opengl.EGLExt.eglPresentationTimeANDROID(eglDisplay, eglSurface, nsecs)
            }
            
            fun swapBuffers(surface: Surface): Boolean {
                val eglSurface = surfaceMap[surface] ?: return false
                return android.opengl.EGL14.eglSwapBuffers(eglDisplay, eglSurface)
            }
            
            fun querySurface(surface: Surface, attribute: Int): Int {
                val eglSurface = surfaceMap[surface] ?: return 0
                val value = IntArray(1)
                android.opengl.EGL14.eglQuerySurface(eglDisplay, eglSurface, attribute, value, 0)
                return value[0]
            }

            fun release() {
                 if (eglDisplay != android.opengl.EGL14.EGL_NO_DISPLAY) {
                    android.opengl.EGL14.eglMakeCurrent(eglDisplay, android.opengl.EGL14.EGL_NO_SURFACE, android.opengl.EGL14.EGL_NO_SURFACE, android.opengl.EGL14.EGL_NO_CONTEXT)
                    
                    surfaceMap.values.forEach { 
                        android.opengl.EGL14.eglDestroySurface(eglDisplay, it)
                    }
                    surfaceMap.clear()
                    
                    android.opengl.EGL14.eglDestroyContext(eglDisplay, eglContext)
                    android.opengl.EGL14.eglTerminate(eglDisplay)
                }
                
                eglDisplay = android.opengl.EGL14.EGL_NO_DISPLAY
                eglContext = android.opengl.EGL14.EGL_NO_CONTEXT
                eglConfig = null
            }
        }
    }
}
