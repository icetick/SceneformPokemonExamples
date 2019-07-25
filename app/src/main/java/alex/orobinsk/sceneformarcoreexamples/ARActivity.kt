package alex.orobinsk.sceneformarcoreexamples

import android.animation.Animator
import android.animation.ObjectAnimator
import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import com.google.ar.core.Anchor
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.collision.Box
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.QuaternionEvaluator
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.*
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import kotlinx.android.synthetic.main.activity_ar.*
import java.util.concurrent.CompletableFuture

class ARActivity : AppCompatActivity() {
    private val MIN_OPENGL_VERSION = 3.0
    private val BITMAP_GL_SIZE = 2048
    private var primaryRenderable: ModelRenderable? = null
    private var currentRenderable: ModelRenderable? = null
    private val evolveMatrix: MutableList<MutableList<Int>> = arrayListOf(
        mutableListOf(R.raw.bulbasaur, R.raw.ivysaur, R.raw.venusaur, R.raw.venusaur_mega),
        mutableListOf(R.raw.charmander, R.raw.charmeleon, R.raw.charizard, R.raw.charizard_mega),
        mutableListOf(R.raw.squirtle, R.raw.wartortle, R.raw.blastoise, R.raw.blastoise_mega)
    )
    private var blockInterruption: Boolean = false

    private val renderableResources: List<Int> =
        listOf(R.raw.beedrill, R.raw.pikachu, R.raw.bulbasaur, R.raw.charmander, R.raw.squirtle)

    private var currentResource: Int = 0

    private lateinit var arFragment: ArFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!checkIsSupportedDeviceOrFinish(this)) {
            return
        }

        setContentView(R.layout.activity_ar)
        arFragment = ux_fragment as ArFragment

        //Creating material texture from jpg
        val bitmap = getGLCompatibleBitmap(R.raw.diffuze)
        val bulbasaurTextureFuture = getTextureFuture(bitmap)
        var bulbasaurMaterialFuture: CompletableFuture<Material>

        bulbasaurTextureFuture.thenAccept {
            bulbasaurMaterialFuture = getMaterialFuture(it)
            renderModel(R.raw.pokemon)
                .thenAcceptBoth(bulbasaurMaterialFuture) { renderableResult, material ->
                    primaryRenderable = renderableResult
                    primaryRenderable?.material = material
                }
        }

        setupSpinnerAdapter()
        setupSpinnerListener()

        arFragment.setOnTapArPlaneListener { hitResult: HitResult, plane: Plane, motionEvent: MotionEvent ->
            primaryRenderable ?: return@setOnTapArPlaneListener

            val anchorNode = createHitAnchor(hitResult)

            currentRenderable?.apply {
                placeObject(anchorNode.anchor, Node.OnTouchListener { hitTestResult, motionEvent ->
                    hitTestResult?.node?.let {
                        if (!blockInterruption) {
                            addInfoPopup(hitTestResult.node)
                        }
                    };true
                })
            }
/*
            pikachuRenderable?.apply {
                placeObject(anchorNode.anchor)
            }

            val node = AnchorNode(
                hitResult.trackable.createAnchor(
                    hitResult.hitPose.compose(Pose.makeTranslation(0.5f, 0f, 0.5f))
                )
            )

            node.setParent(arFragment.arSceneView.scene)

            beedrillRenderable?.apply {
                placeObject(node.anchor)
            }

            val nodePrimary = AnchorNode(
                hitResult.trackable.createAnchor(
                    hitResult.hitPose.compose(Pose.makeTranslation(0.5f, 0f, 0.5f))
                )
            )

            nodePrimary.setParent(arFragment.arSceneView.scene)

            primaryRenderable?.apply {
                placeObject(nodePrimary.anchor, Node.OnTouchListener { hitTestResult, motionEvent ->
                    hitTestResult?.node?.let {
                        addInfoPopup(hitTestResult.node)
                    };true
                })
            }*/
        }
    }

    private fun setupSpinnerListener() {
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                renderModel(renderableResources[position])
                    .thenAccept {
                        currentRenderable = it
                        currentResource = renderableResources[position]
                    }
            }
        }
    }

    private fun setupSpinnerAdapter() {
        ArrayAdapter.createFromResource(this, R.array.pokemons, android.R.layout.simple_spinner_item)
            .also {
                it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinner.adapter = it
            }
    }

    private fun createHitAnchor(hitResult: HitResult): AnchorNode {
        val anchor = hitResult.createAnchor()
        val anchorNode = AnchorNode(anchor)
        anchorNode.setParent(arFragment.arSceneView.scene)
        return anchorNode
    }

    private fun renderModel(model: Int): CompletableFuture<ModelRenderable> {
        return ModelRenderable.builder()
            .setSource(this, model)
            .setRegistryId(model)
            .build()
            .exceptionally {
                Toast.makeText(this@ARActivity, "Could not fetch model from $model", Toast.LENGTH_SHORT).show()
                return@exceptionally null
            }
    }

    private fun getMaterialFuture(it: Texture?): CompletableFuture<Material> {
        return MaterialFactory.makeOpaqueWithTexture(this, it)
    }

    private fun getTextureFuture(bitmap: Bitmap): CompletableFuture<Texture> {
        return Texture.builder().setSource(bitmap)
            .build()
    }

    private fun getGLCompatibleBitmap(res: Int): Bitmap {
        return Bitmap.createScaledBitmap(
            BitmapFactory.decodeResource(resources, res),
            BITMAP_GL_SIZE,
            BITMAP_GL_SIZE,
            true
        )
    }

    private fun addInfoPopup(poke: Node?) {
        var popupNode = Node()
        popupNode.setParent(poke)
        val popupSpaceSize = 0.01f

        popupNode.localPosition = Vector3(0f, popupSpaceSize + (poke?.getHeight()?:0f), 0f)

        ViewRenderable.builder().setView(this, R.layout.info_card).build()
            .thenAccept { renderable: ViewRenderable? ->
                run {
                    popupNode.renderable = renderable
                    var orbitAnimation = createAnimator()
                    orbitAnimation.target = poke

                    //Make animation for one second
                    val animationDuration: Long = 1000
                    orbitAnimation.duration = animationDuration

                    orbitAnimation.start()

                    (renderable?.view as TextView?)?.text = "Evolving"

                    var evolveModel: ModelRenderable? = null

                    renderable?.view?.animate()?.alpha(0.0f)?.setDuration(animationDuration)
                        ?.setListener(object : Animator.AnimatorListener {
                            override fun onAnimationRepeat(animation: Animator?) {

                            }

                            override fun onAnimationEnd(animation: Animator?) {
                                poke?.removeChild(popupNode)
                                evolveModel.let {
                                    poke?.renderable = evolveModel
                                }
                                blockInterruption = false
                            }

                            override fun onAnimationCancel(animation: Animator?) {
                            }

                            override fun onAnimationStart(animation: Animator?) {
                                blockInterruption = true
                                val evolution = getNextEvo()
                                if (evolution != 0) {
                                    renderModel(evolution)
                                        .thenAccept {
                                            evolveModel = it
                                            currentResource = evolution
                                        }
                                }
                            }

                        })
                }
            }
    }

    private fun Node.getHeight(): Float {
       return (this.renderable?.collisionShape as Box).size.y
    }

    private fun getNextEvo(): Int {
        try {
            for (item in evolveMatrix) {
                if (item.contains(currentResource)) {
                    return item[item.indexOf(currentResource) + 1]
                }
            }
        } catch (ex: Exception) {
            for (item in evolveMatrix) {

                if (item.contains(currentResource)) {
                    return item[0]
                }

            }
        }
        return 0
    }

    private fun createAnimator(): ObjectAnimator {
        //Rotate over Y axis by 360
        var orientation1 = Quaternion.axisAngle(Vector3(0.0f, 1.0f, 0.0f), 0f)
        var orientation2 = Quaternion.axisAngle(Vector3(0.0f, 1.0f, 0.0f), 120f)
        var orientation3 = Quaternion.axisAngle(Vector3(0.0f, 1.0f, 0.0f), 240f)
        var orientation4 = Quaternion.axisAngle(Vector3(0.0f, 1.0f, 0.0f), 360f)

        var orbitAnimation = ObjectAnimator()
        orbitAnimation.setObjectValues(orientation1, orientation2,orientation3,orientation4)
        orbitAnimation.propertyName = "localRotation"
        orbitAnimation.setEvaluator(QuaternionEvaluator())
        orbitAnimation.interpolator = LinearInterpolator()
        orbitAnimation.setAutoCancel(true)

        return orbitAnimation
    }

    private fun makeTextureSphere(hitResult: HitResult, res: Int) {
        val bitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(resources, res), 512, 512, true)
        Texture.builder().setSource(bitmap)
            .build()
            .thenAccept {
                MaterialFactory.makeOpaqueWithTexture(this, it)
                    .thenAccept { material ->
                        ShapeFactory.makeSphere(
                            0.1f,
                            Vector3(0.0f, 0.15f, 0.0f),
                            material
                        ).apply {
                            placeObject(hitResult.createAnchor())
                        }
                    }
            }
    }

    private fun ModelRenderable.placeObject(anchor: Anchor) {
        val anchorNode = AnchorNode(anchor)

        TransformableNode(arFragment.transformationSystem).apply {
            renderable = this@placeObject
            setParent(anchorNode)
            select()
        }

        arFragment.arSceneView.scene.addChild(anchorNode)
    }

    private fun ModelRenderable.placeObject(anchor: Anchor?, listener: Node.OnTouchListener) {
        val anchorNode = AnchorNode(anchor)

        TransformableNode(arFragment.transformationSystem).apply {
            renderable = this@placeObject
            setParent(anchorNode)
            setOnTouchListener(listener)
            select()
        }

        arFragment.arSceneView.scene.addChild(anchorNode)
    }


    private fun checkIsSupportedDeviceOrFinish(activity: Activity): Boolean {
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
}
