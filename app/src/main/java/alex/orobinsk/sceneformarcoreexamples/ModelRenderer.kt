package alex.orobinsk.sceneformarcoreexamples

import alex.orobinsk.sceneformarcoreexamples.Utils.placeObject
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.Toast
import com.google.ar.core.HitResult
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.MaterialFactory
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.ShapeFactory
import com.google.ar.sceneform.rendering.Texture
import com.google.ar.sceneform.ux.ArFragment
import java.util.concurrent.CompletableFuture

class ModelRenderer constructor(val context: Context) {
    fun renderModel(model: Int): CompletableFuture<ModelRenderable> {
        return ModelRenderable.builder()
            .setSource(context, model)
            .setRegistryId(model)
            .build()
            .exceptionally {
                Toast.makeText(context, "Could not fetch model from $model", Toast.LENGTH_SHORT).show()
                return@exceptionally null
            }
    }

    private fun makeTextureSphere(arFragment: ArFragment, hitResult: HitResult, res: Int) {
        val bitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(context.resources, res), 512, 512, true)
        Texture.builder().setSource(bitmap)
            .build()
            .thenAccept {
                MaterialFactory.makeOpaqueWithTexture(context, it)
                    .thenAccept { material ->
                        ShapeFactory.makeSphere(
                            0.1f,
                            Vector3(0.0f, 0.15f, 0.0f),
                            material
                        ).apply {
                            placeObject(arFragment, hitResult.createAnchor())
                        }
                    }
            }
    }
}