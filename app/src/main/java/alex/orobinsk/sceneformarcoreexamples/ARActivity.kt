package alex.orobinsk.sceneformarcoreexamples

import alex.orobinsk.sceneformarcoreexamples.Utils.createHitAnchor
import alex.orobinsk.sceneformarcoreexamples.Utils.placeObject
import android.arch.lifecycle.MutableLiveData
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MotionEvent
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import kotlinx.android.synthetic.main.activity_ar.*

class ARActivity : AppCompatActivity() {
    private var currentRenderable: ModelRenderable? = null
    private var blockAnimationInterruption: MutableLiveData<Boolean> = MutableLiveData()
    private var currentResource: MutableLiveData<Int> = MutableLiveData()

    private lateinit var arFragment: ArFragment
    private lateinit var modelRenderer: ModelRenderer
    private lateinit var modelProvider: ModelProvider
    private lateinit var popupHelper: PopupHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!Utils.checkIsSupportedDeviceOrFinish(this)) {
            return
        }
        blockAnimationInterruption.value = false

        setContentView(R.layout.activity_ar)
        arFragment = ux_fragment as ArFragment
        modelRenderer = ModelRenderer(this)
        modelProvider = ModelProvider()
        popupHelper = PopupHelper(this, modelProvider, modelRenderer)

        setupSpinnerAdapter()
        setupSpinnerListener()

        arFragment.setOnTapArPlaneListener { hitResult: HitResult, plane: Plane, motionEvent: MotionEvent ->
            val anchorNode = createHitAnchor(arFragment, hitResult)

            currentRenderable?.apply {
               placeObject(arFragment, anchorNode.anchor, Node.OnTouchListener { hitTestResult, motionEvent ->
                    hitTestResult?.node?.let {
                        blockAnimationInterruption.value?.let { blockFlag ->
                            if (!blockFlag) {
                                popupHelper.addInfoPopup(hitTestResult.node, currentResource, blockAnimationInterruption)
                            }
                        }
                    };true
                })
            }
        }
    }

    private fun setupSpinnerListener() {
        val renderableResources = ModelProvider().provideCommonRenderables()

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                modelRenderer.renderModel(renderableResources[position])
                    .thenAccept {
                        currentRenderable = it
                        currentResource.value = renderableResources[position]
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
}
