package alex.orobinsk.sceneformarcoreexamples

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.google.ar.sceneform.rendering.Material
import com.google.ar.sceneform.rendering.MaterialFactory
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.Texture
import java.util.concurrent.CompletableFuture

class UVTextureMaker(val context: Context) {
    private var primaryRenderable: ModelRenderable? = null
    private val BITMAP_GL_SIZE = 2048
    private lateinit var modelRenderer: ModelRenderer

    init {
        modelRenderer = ModelRenderer(context)
        var bulbasaurMaterialFuture: CompletableFuture<Material>

        val bitmap = getGLCompatibleBitmap(R.raw.diffuze)
        val bulbasaurTextureFuture = getTextureFuture(bitmap)

        bulbasaurTextureFuture.thenAccept {
            bulbasaurMaterialFuture = getMaterialFuture(it)
            modelRenderer.renderModel(R.raw.pokemon)
                .thenAcceptBoth(bulbasaurMaterialFuture) { renderableResult, material ->
                    primaryRenderable = renderableResult
                    primaryRenderable?.material = material
                }
        }
    }

    private fun getTextureFuture(bitmap: Bitmap): CompletableFuture<Texture> {
        return Texture.builder().setSource(bitmap)
            .build()
    }

    private fun getGLCompatibleBitmap(res: Int): Bitmap {
        return Bitmap.createScaledBitmap(
            BitmapFactory.decodeResource(context.resources, res),
            BITMAP_GL_SIZE,
            BITMAP_GL_SIZE,
            true
        )
    }
    private fun getMaterialFuture(it: Texture?): CompletableFuture<Material> {
        return MaterialFactory.makeOpaqueWithTexture(context, it)
    }
}