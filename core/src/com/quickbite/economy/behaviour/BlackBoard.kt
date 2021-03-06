package com.quickbite.economy.behaviour

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.math.Vector2
import com.quickbite.economy.components.BuildingComponent
import com.quickbite.economy.util.objects.FarmObject
import com.quickbite.economy.util.objects.ItemAmountLink


class BlackBoard{
    lateinit var myself:Entity
    var path:List<Vector2> = listOf()
    var currPathIndex:Int = 0

    var targetPosition = Vector2()
    var targetEntity:Entity? = null
    var targetBuilding:BuildingComponent? = null
    var targetPlantSpot: FarmObject? = null

    /** A hashset of entities to ignore. Multipurpose*/
    var entitiesToIgnore:HashSet<Entity> = hashSetOf()

    /** Can be used for things like checking if we are already inside a building*/
    var insideEntity:Entity? = null

    val targetItem = ItemAmountLink("", 0)
}