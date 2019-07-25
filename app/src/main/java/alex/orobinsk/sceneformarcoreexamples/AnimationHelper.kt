package alex.orobinsk.sceneformarcoreexamples

import android.animation.Animator
import android.animation.ObjectAnimator
import android.view.animation.LinearInterpolator
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.QuaternionEvaluator
import com.google.ar.sceneform.math.Vector3

class AnimationHelper {
    fun createAnimator(): ObjectAnimator {
        //Rotate over Y axis by 360
        var orientation1 = Quaternion.axisAngle(Vector3(0.0f, 1.0f, 0.0f), 180f)
        var orientation2 = Quaternion.axisAngle(Vector3(0.0f, 1.0f, 0.0f), 270f)
        var orientation3 = Quaternion.axisAngle(Vector3(0.0f, 1.0f, 0.0f), 360f)
        var orientation4 = Quaternion.axisAngle(Vector3(0.0f, 1.0f, 0.0f), 90f)
        var orientation5 = Quaternion.axisAngle(Vector3(0.0f, 1.0f, 0.0f), 180f)

        var orbitAnimation = ObjectAnimator()
        orbitAnimation.setObjectValues(orientation1, orientation2, orientation3, orientation4, orientation5)
        orbitAnimation.propertyName = "localRotation"
        orbitAnimation.setEvaluator(QuaternionEvaluator())
        orbitAnimation.interpolator = LinearInterpolator()
        orbitAnimation.setAutoCancel(true)

        return orbitAnimation
    }
}