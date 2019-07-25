package alex.orobinsk.sceneformarcoreexamples

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.util.Log
import android.widget.Toast
import com.google.ar.core.Anchor
import com.google.ar.core.HitResult
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.collision.Box
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode

object Utils {
    private val MIN_OPENGL_VERSION = 3.0

    fun checkIsSupportedDeviceOrFinish(activity: Activity): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            Log.e("Sceneform - ", "Sceneform requires Android N or later")
            Toast.makeText(activity, "Sceneform requires Android N or later", Toast.LENGTH_LONG).show()
            activity.finish()
            return false
        }
        val openGlVersionString = (activity.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager)
            .deviceConfigurationInfo
            .glEsVersion
        if (java.lang.Double.parseDouble(openGlVersionString) < MIN_OPENGL_VERSION) {
            Log.e("Sceneform - ", "Sceneform requires OpenGL ES 3.0 later")
            Toast.makeText(activity, "Sceneform requires OpenGL ES 3.0 or later", Toast.LENGTH_LONG)
                .show()
            activity.finish()
            return false
        }
        return true
    }

    fun ModelRenderable.placeObject(arFragment: ArFragment, anchor: Anchor) {
        val anchorNode = AnchorNode(anchor)

        TransformableNode(arFragment.transformationSystem).apply {
            renderable = this@placeObject
            setParent(anchorNode)
            localRotation = Quaternion.axisAngle(Vector3(0.0f, 1.0f, 0.0f), 180f)
            select()
        }

        arFragment.arSceneView.scene.addChild(anchorNode)
    }

    fun ModelRenderable.placeObject(arFragment: ArFragment, anchor: Anchor?, listener: Node.OnTouchListener) {
        val anchorNode = AnchorNode(anchor)


        TransformableNode(arFragment.transformationSystem).apply {
            renderable = this@placeObject
            setParent(anchorNode)
            setOnTouchListener(listener)
            localRotation = Quaternion.axisAngle(Vector3(0.0f, 1.0f, 0.0f), 180f)
            select()
        }

        arFragment.arSceneView.scene.addChild(anchorNode)
    }

    fun createHitAnchor(arFragment: ArFragment, hitResult: HitResult): AnchorNode {
        val anchor = hitResult.createAnchor()
        val anchorNode = AnchorNode(anchor)
        anchorNode.setParent(arFragment.arSceneView.scene)
        return anchorNode
    }

    fun Node?.getHeight(): Float {
        return (this?.renderable?.collisionShape as Box).size.y
    }
}