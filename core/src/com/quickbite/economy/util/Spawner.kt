package com.quickbite.economy.util

import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.quickbite.economy.behaviour.Tasks
import com.quickbite.economy.components.BuildingComponent
import com.quickbite.economy.managers.ItemDefManager
import com.quickbite.economy.managers.TownManager
import com.quickbite.economy.objects.Town

/**
 * Created by Paha on 1/22/2017.
 */
object Spawner {
    val town:Town by lazy { TownManager.getTown("Town") }

    val spawnPosition = Vector2(-500f, 0f)

    val spawnBuyerTimeRange = Vector2(1f, 10f)
    val spawnHaulerTimeRange = Vector2(5f, 20f)

    const val populationMultiplierForBuyerThreshold = 200 //For every x amount of population, increase the multiplier by 1
    val populationMultiplierForBuyer:Float
        get() = Math.max(1f, town.population.toFloat() / populationMultiplierForBuyerThreshold.toFloat()) //We want this to be at least 1

    const val populationMultiplierForHaulerThreshold = 400 //For every x amount of population, increase the multiplier by 1
    val populationMultiplierForHauler:Float
        get() = Math.max(1f, town.population.toFloat() / populationMultiplierForHaulerThreshold.toFloat()) //We want this to be at least 1

    lateinit var spawnBuyerTimer:CustomTimer
    lateinit var spawnHaulerTimer:CustomTimer

    init{
        spawnBuyerTimer = CustomTimer(MathUtils.random(spawnBuyerTimeRange.x, spawnBuyerTimeRange.y) / populationMultiplierForBuyer, true, {
            val list = ItemDefManager.itemDefMap.values.toList() //Get the list of items
            val randomItem = list[MathUtils.random(list.size-1)] //Randomly pick an item
            val itemToBuy = ItemAmountLink(randomItem.itemName, MathUtils.random(1, 10)) //Get an item to buy

            //If we found a building that is selling it, spawn a buyer
            if(Util.getClosestSellingItem(spawnPosition, itemToBuy.itemName) != null){
                //Randomly assign an item and amount wanted
                val entity = Factory.createObjectFromJson("buyer", spawnPosition)
                val buying = Mappers.buyer[entity]
                val inventory = Mappers.inventory[entity]

                buying.buyList.add(itemToBuy)
                inventory.addItem("Gold", MathUtils.random(500, 1000))

                //Scan each item that we are buying and calculate necessity and luxury ratings
                buying.buyList.forEach { (itemName, itemAmount) ->
                    val itemDef = ItemDefManager.itemDefMap[itemName]!!
                    if(itemDef.category == "Food")
                        buying.needsSatisfactionRating -= itemAmount
                    //TODO Calculate luxury rating
                }

            //Otherwise, if nothing is selling this item...
            }else{
                //If it's a food (need) item, subtract from the town
                val category = ItemDefManager.itemDefMap[itemToBuy.itemName]!!.category
                if(category == "Food"){
                    town.needsRating -= itemToBuy.itemAmount
                }
            }

            spawnBuyerTimer.restart(MathUtils.random(spawnBuyerTimeRange.x, spawnBuyerTimeRange.y) / populationMultiplierForHauler)
        })

        spawnHaulerTimer = CustomTimer(MathUtils.random(spawnHaulerTimeRange.x, spawnHaulerTimeRange.y), true, {
            val list = town.itemIncomeMap.values.toList()
            val randomItem = list[MathUtils.random(list.size - 1)]
            val itemToBuy = ItemAmountLink(randomItem.itemName, MathUtils.random(1, randomItem.accumulatedItemCounter.toInt()))
            randomItem.accumulatedItemCounter -= itemToBuy.itemAmount

            if(Util.getClosestBuildingType(spawnPosition, BuildingComponent.BuildingType.Stockpile) != null){
                //Randomly assign an item and amount wanted
                val entity = Factory.createObjectFromJson("hauler", spawnPosition)
                val inventory = Mappers.inventory[entity]
                val beh = Mappers.behaviour[entity]

                inventory.addItem(itemToBuy.itemName, itemToBuy.itemAmount)

                beh.currTask = Tasks.haulInventoryToStockpile(beh.blackBoard)
            }

            spawnHaulerTimer.restart(MathUtils.random(spawnHaulerTimeRange.x, spawnHaulerTimeRange.y))
        })
    }

    fun update(delta:Float){
        spawnBuyerTimer.update(delta)
        spawnHaulerTimer.update(delta)
    }
}