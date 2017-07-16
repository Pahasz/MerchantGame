package com.quickbite.economy.systems

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.utils.ImmutableArray
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.Vector2
import com.quickbite.economy.components.GraphicComponent
import com.quickbite.economy.components.TransformComponent
import com.quickbite.economy.util.Mappers
import java.util.*

/**
 * Created by Paha on 1/16/2017.
 * An EntitySystem that handles drawing graphics
 */
class RenderSystem(val batch:SpriteBatch) : EntitySystem(){
    lateinit var entities: ImmutableArray<Entity>
    lateinit var sortedEntities:List<Entity>

    val bounceOut: Interpolation.BounceOut = Interpolation.bounceOut

    override fun addedToEngine(engine: Engine) {
        super.addedToEngine(engine)

        val family = Family.all(GraphicComponent::class.java, TransformComponent::class.java).get()
        entities = engine.getEntitiesFor(family)
    }

    override fun update(deltaTime: Float) {
        super.update(deltaTime)

        //TODO This could possibly get super laggy. Might need a better way to deal with this...
        sortedEntities = entities.sortedWith(Comparator<Entity> { o1, o2 ->
            val tc1 = Mappers.transform[o1]
            val tc2 = Mappers.transform[o2]
            if(tc2.position.y - tc1.position.y < 0)
                -1
            else if(tc2.position.y - tc1.position.y > 0)
                1
            else
                0
        })

        sortedEntities.forEach { ent ->
            val gc = Mappers.graphic.get(ent)
            val tc = Mappers.transform.get(ent)
            val pc = Mappers.preview.get(ent)
            val fc = Mappers.farm[ent]

            if(pc != null)
                gc.sprite.setAlpha(0.2f)

            gc.sprite.setPosition(tc.position.x - gc.anchor.x*gc.sprite.width, tc.position.y - gc.anchor.y*gc.sprite.height)

            if(gc.initialAnimation && pc == null){
                runAnimation(deltaTime, gc, tc)
            }

            if(!gc.hide)
                gc.sprite.draw(batch)

            if(fc != null){
                fc.plantSpots.forEach { it.forEach { (position, _, sprite) ->
                    sprite.setPosition(position.x + tc.position.x - sprite.width/2f, position.y + tc.position.y - sprite.height/2f)
                    sprite.draw(batch)
                }}
            }
        }
    }

    private fun runAnimation(delta:Float, gc:GraphicComponent, tc:TransformComponent){
        val pos = Vector2(tc.position.x, tc.position.y + 25)
        val out = bounceOut.apply(pos.y, tc.position.y, gc.animationCounter)

        gc.animationCounter += delta
        gc.sprite.setPosition(tc.position.x - gc.anchor.x*gc.sprite.width, out - gc.anchor.y*gc.sprite.height)

        if(gc.animationCounter >= 1f)
            gc.initialAnimation = false
    }
}