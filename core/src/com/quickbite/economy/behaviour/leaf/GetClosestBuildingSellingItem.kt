package com.quickbite.economy.behaviour.leaf

import com.badlogic.gdx.math.Vector2
import com.quickbite.economy.behaviour.BlackBoard
import com.quickbite.economy.behaviour.LeafTask
import com.quickbite.economy.util.FindEntityUtil
import com.quickbite.economy.util.Mappers
import com.quickbite.economy.util.Util

/**
 * Created by Paha on 1/19/2017.
 */
class GetClosestBuildingSellingItem(bb:BlackBoard, var itemName:String = "") : LeafTask(bb){

    override fun start() {
        super.start()

        if(itemName == ""){
            itemName = bb.targetItem.itemName
        }

        val building = FindEntityUtil.getClosestSellingItem(Mappers.transform.get(bb.myself).position, itemName)
        if(building!= null){
            bb.targetPosition = Vector2(Mappers.transform.get(building).position)
            bb.targetEntity = building
            bb.targetBuilding = Mappers.building.get(building)

            controller.finishWithSuccess()
        }else
            controller.finishWithFailure()
    }
}