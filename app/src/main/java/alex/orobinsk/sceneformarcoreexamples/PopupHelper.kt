package alex.orobinsk.sceneformarcoreexamples

import alex.orobinsk.sceneformarcoreexamples.Utils.getHeight
import android.animation.Animator
import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.widget.TextView
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.ViewRenderable

class PopupHelper(val context: Context,val modelProvider: ModelProvider, val modelRenderer: ModelRenderer) {
    fun addInfoPopup(poke: Node?, currentResource: MutableLiveData<Int>, blockAnimationInterruption: MutableLiveData<Boolean>) {
        var popupNode = Node()
        popupNode.setParent(poke)
        val popupSpaceSize = 0.01f

        popupNode.localPosition = Vector3(0f, popupSpaceSize + (poke?.getHeight() ?: 0f), 0f)

        ViewRenderable.builder().setView(context, R.layout.info_card).build()
            .thenAccept { renderable: ViewRenderable? ->
                run {
                    popupNode.renderable = renderable
                    var orbitAnimation = AnimationHelper().createAnimator()
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
                                blockAnimationInterruption.value = false
                            }

                            override fun onAnimationCancel(animation: Animator?) {
                            }

                            override fun onAnimationStart(animation: Animator?) {
                                blockAnimationInterruption.value = true
                                val evolution = modelProvider.provideNextEvo(currentResource.value)
                                if (evolution != 0) {
                                    modelRenderer.renderModel(evolution)
                                        .thenAccept {
                                            evolveModel = it
                                            currentResource.value = evolution
                                        }
                                }
                            }

                        })
                }
            }
    }
}