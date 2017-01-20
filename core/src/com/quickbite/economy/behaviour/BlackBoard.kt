package com.quickbite.economy.behaviour

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.math.Vector2
import com.quickbite.economy.components.BuildingComponent


class BlackBoard{
    lateinit var myself:Entity
    var path:List<Vector2> = listOf()

    var targetPosition = Vector2()
    var targetEntity:Entity? = null
    var targetBuilding:BuildingComponent? = null
}