package com.kaimera.tablet.rendering

import android.opengl.GLES11Ext
import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class TextureRenderer {
    
    private val vertexShaderCode = """
        attribute vec4 aPosition;
        attribute vec4 aTextureCoord;
        varying vec2 vTextureCoord;
        uniform mat4 uTexMatrix;
        void main() {
            gl_Position = aPosition;
            vTextureCoord = (uTexMatrix * aTextureCoord).xy;
        }
    """

    private val defaultFragmentShaderCode = """
        #extension GL_OES_EGL_image_external : require
        precision mediump float;
        varying vec2 vTextureCoord;
        uniform samplerExternalOES sTexture;
        void main() {
            gl_FragColor = texture2D(sTexture, vTextureCoord);
        }
    """
    
    private val bwFragmentShaderCode = """
        #extension GL_OES_EGL_image_external : require
        precision mediump float;
        varying vec2 vTextureCoord;
        uniform samplerExternalOES sTexture;
        void main() {
            vec4 color = texture2D(sTexture, vTextureCoord);
            float gray = dot(color.rgb, vec3(0.299, 0.587, 0.114));
            gl_FragColor = vec4(gray, gray, gray, color.a);
        }
    """

    private val sepiaFragmentShaderCode = """
        #extension GL_OES_EGL_image_external : require
        precision mediump float;
        varying vec2 vTextureCoord;
        uniform samplerExternalOES sTexture;
        void main() {
            vec4 color = texture2D(sTexture, vTextureCoord);
            float r = color.r;
            float g = color.g;
            float b = color.b;
            color.r = dot(vec3(r, g, b), vec3(0.393, 0.769, 0.189));
            color.g = dot(vec3(r, g, b), vec3(0.349, 0.686, 0.168));
            color.b = dot(vec3(r, g, b), vec3(0.272, 0.534, 0.131));
            gl_FragColor = color;
        }
    """

    private val cyberpunkFragmentShaderCode = """
        #extension GL_OES_EGL_image_external : require
        precision mediump float;
        varying vec2 vTextureCoord;
        uniform samplerExternalOES sTexture;
        void main() {
            vec4 color = texture2D(sTexture, vTextureCoord);
            vec3 teal = vec3(0.0, 1.0, 1.0);
            vec3 orange = vec3(1.0, 0.5, 0.0);
            float gray = dot(color.rgb, vec3(0.299, 0.587, 0.114));
            
            vec3 mixedColor = mix(teal, orange, gray);
            gl_FragColor = vec4(mix(color.rgb, mixedColor, 0.3), color.a);
        }
    """
    
    private val vividFragmentShaderCode = """
        #extension GL_OES_EGL_image_external : require
        precision mediump float;
        varying vec2 vTextureCoord;
        uniform samplerExternalOES sTexture;
        void main() {
            vec4 color = texture2D(sTexture, vTextureCoord);
            float gray = dot(color.rgb, vec3(0.299, 0.587, 0.114));
            vec3 grayColor = vec3(gray, gray, gray);
            gl_FragColor = vec4(mix(grayColor, color.rgb, 1.5), color.a);
        }
    """

    private val vertexBuffer: FloatBuffer
    private val mTriangleVerticesData = floatArrayOf(
        -1.0f, -1.0f, 0f, 0f, 0f,
        1.0f, -1.0f, 0f, 1f, 0f,
        -1.0f, 1.0f, 0f, 0f, 1f,
        1.0f, 1.0f, 0f, 1f, 1f
    )

    private var mProgram = 0
    private var muMVPMatrixHandle = 0
    private var muTexMatrixHandle = 0
    private var maPositionHandle = 0
    private var maTextureHandle = 0
    private var currentFilterType = FilterType.NORMAL

    enum class FilterType {
        NORMAL, BW, SEPIA, CYBERPUNK, VIVID
    }

    init {
        vertexBuffer = ByteBuffer.allocateDirect(mTriangleVerticesData.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
        vertexBuffer.put(mTriangleVerticesData).position(0)
    }

    fun setFilter(type: FilterType) {
        if (currentFilterType != type) {
            currentFilterType = type
             mProgram = 0 
        }
    }

    fun draw(texId: Int, texMatrix: FloatArray) {
        if (mProgram == 0) {
            val fragmentShader = when (currentFilterType) {
                FilterType.NORMAL -> defaultFragmentShaderCode
                FilterType.BW -> bwFragmentShaderCode
                FilterType.SEPIA -> sepiaFragmentShaderCode
                FilterType.CYBERPUNK -> cyberpunkFragmentShaderCode
                FilterType.VIVID -> vividFragmentShaderCode
            }
            mProgram = ShaderUtils.createProgram(vertexShaderCode, fragmentShader)
            if (mProgram == 0) return
            
            maPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition")
            maTextureHandle = GLES20.glGetAttribLocation(mProgram, "aTextureCoord")
            muMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix")
            muTexMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uTexMatrix")
        }

        GLES20.glUseProgram(mProgram)

        vertexBuffer.position(0)
        GLES20.glVertexAttribPointer(
            maPositionHandle, 3, GLES20.GL_FLOAT, false,
            20, vertexBuffer
        )
        GLES20.glEnableVertexAttribArray(maPositionHandle)

        vertexBuffer.position(3)
        GLES20.glVertexAttribPointer(
            maTextureHandle, 2, GLES20.GL_FLOAT, false,
            20, vertexBuffer
        )
        GLES20.glEnableVertexAttribArray(maTextureHandle)

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texId)
        
        val sTextureHandle = GLES20.glGetUniformLocation(mProgram, "sTexture")
        if (sTextureHandle != -1) {
             GLES20.glUniform1i(sTextureHandle, 0)
        }

        GLES20.glUniformMatrix4fv(muTexMatrixHandle, 1, false, texMatrix, 0)
        
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
        
        GLES20.glDisableVertexAttribArray(maPositionHandle)
        GLES20.glDisableVertexAttribArray(maTextureHandle)
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0)
        GLES20.glUseProgram(0)
    }
    
    fun release() {
        if (mProgram != 0) {
            GLES20.glDeleteProgram(mProgram)
            mProgram = 0
        }
    }
}
