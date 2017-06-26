package com.quickbite.economy.behaviour.leaf

import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.quickbite.economy.behaviour.BlackBoard
import com.quickbite.economy.behaviour.LeafTask
import com.quickbite.economy.util.Constants
import com.quickbite.economy.util.Mappers

/**
 * Created by Paha on 5/29/2017.
 *
 * Enters a building, which will move the Entity into the building with a quick fade.
 */
class EnterBuilding(bb: BlackBoard) : LeafTask(bb) {
    var moveTime:Float = 0f
    var fadeTime:Float = 0f
    var counter = 0f

    val ic by lazy { Mappers.graphic[bb.myself] }
    val tc by lazy { Mappers.transform[bb.myself] }
    val vc by lazy { Mappers.velocity[bb.myself] }
    val destPos = Vector2()
    val bc by lazy { Mappers.body[bb.myself] }
    val startPos = Vector2()

    override fun start() {
        super.start()

        destPos.set(Vector2(Mappers.transform[bb.targetEntity].position))

        //Scale both of these to box2D coordinates since we'll be manipulating bodies
        startPos.set(tc.position.x* Constants.BOX2D_SCALE, tc.position.y*Constants.BOX2D_SCALE)
        destPos.set(destPos.x* Constants.BOX2D_SCALE, destPos.y*Constants.BOX2D_SCALE)

        moveTime = startPos.dst(destPos)/(vc.baseSpeed*Constants.BOX2D_SCALE)
        fadeTime = moveTime*0.3f

        println("move time $moveTime")
    }

    override fun update(delta: Float) {
        super.update(delta)

        counter += delta
        val moveAlpha = MathUtils.clamp(counter/ moveTime, 0f, 1f)
        val fadeAlpha = MathUtils.clamp(counter/ fadeTime, 0f, 1f)
        ic.sprite.setAlpha(1 - fadeAlpha)
        bc.body!!.setTransform(Vector2(MathUtils.lerp(startPos.x, destPos.x, moveAlpha), MathUtils.lerp(startPos.y, destPos.y, moveAlpha)), 0f)

        if(counter >= moveTime){
            ic.sprite.setAlpha(0f)
            bb.insideEntity = bb.targetEntity
            this.controller.finishWithSuccess()
        }

    }
}