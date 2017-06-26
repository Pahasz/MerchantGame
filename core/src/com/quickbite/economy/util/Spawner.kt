package com.quickbite.economy.util

import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.quickbite.economy.behaviour.Tasks
import com.quickbite.economy.components.BuildingComponent
import com.quickbite.economy.managers.DefinitionManager
import com.quickbite.economy.managers.TownManager
import com.quickbite.economy.objects.ItemAmountLink
import com.quickbite.economy.objects.SellingItemData
import com.quickbite.economy.objects.Town

/**
 * Created by Paha on 1/22/2017.
 */
object Spawner {
    private val town:Town by lazy { TownManager.getTown("Town") }

    private val spawnPosition = Vector2(-500f, 0f)

    private val spawnBuyerTimeRange = Vector2(1f, 5f)
    private val spawnHaulerTimeRange = Vector2(5f, 20f)

    private const val populationMultiplierForBuyerThreshold = 200 //For every x amount of population, increase the multiplier by 1
    private val populationMultiplierForBuyer:Float
        get() = Math.max(1f, town.population / populationMultiplierForBuyerThreshold.toFloat()) //We want this to be at least 1

    private const val populationMultiplierForHaulerThreshold = 400 //For every x amount of population, increase the multiplier by 1
    private val populationMultiplierForHauler:Float
        get() = Math.max(1f, town.population.toFloat() / populationMultiplierForHaulerThreshold.toFloat()) //We want this to be at least 1

    private lateinit var spawnBuyerTimer:CustomTimer
    private lateinit var spawnHaulerTimer:CustomTimer

    init{
        spawnBuyerTimer = CustomTimer(20f, MathUtils.random(spawnBuyerTimeRange.x, spawnBuyerTimeRange.y) / populationMultiplierForBuyer, true, {
            //TODO Can this be abused by simply deleting all selling buildings? But then what's the fun of the game...
            spawnBuyer()

            spawnBuyerTimer.restart(MathUtils.random(spawnBuyerTimeRange.x, spawnBuyerTimeRange.y) / populationMultiplierForHauler)
        })

        spawnHaulerTimer = CustomTimer(10f, MathUtils.random(spawnHaulerTimeRange.x, spawnHaulerTimeRange.y), true, {
            val list = town.itemImportMap.values.toList()
            val randomItem = list[MathUtils.random(list.size - 1)]

            //Get the closest building that is reselling our item via import
            val closest = FindEntityUtil.getClosestBuildingType(spawnPosition, BuildingComponent.BuildingType.Shop, {
                val selling = Mappers.selling[it]
                selling.resellingItemsList.any { it.itemName == randomItem.itemName && it.itemSourceType == SellingItemData.ItemSource.Import }
            })

            //If we did find a building...
            if(closest != null){
                //Randomly assign an item and amount wanted
                val entity = Factory.createObjectFromJson("hauler", spawnPosition)
                val inventory = Mappers.inventory[entity]
                val beh = Mappers.behaviour[entity]

                //This case is if we have a hauler that tries to spawn too quickly
                if(randomItem.accumulatedItemCounter <= 1)
                    return@CustomTimer

                val itemToBuy = ItemAmountLink(randomItem.itemName, MathUtils.random(1, randomItem.accumulatedItemCounter.toInt()))
                randomItem.accumulatedItemCounter -= itemToBuy.itemAmount

                inventory.addItem(itemToBuy.itemName, itemToBuy.itemAmount)

                beh.blackBoard.targetItem.set(randomItem.itemName, 1) //Item amount doesn't matter here
                beh.currTask = Tasks.haulInventoryToStockpile(beh.blackBoard)
            }

            spawnHaulerTimer.restart(MathUtils.random(spawnHaulerTimeRange.x, spawnHaulerTimeRange.y))
        })
    }

    fun update(delta:Float){
        spawnBuyerTimer.update(delta)
        spawnHaulerTimer.update(delta)
    }

    fun spawnBuyer(){
        if(town.totalSellingItemMap.isNotEmpty()) {
            val list = town.totalSellingItemMap.toList()
            val numItemTypesToBuy = MathUtils.random(1, Math.max(2, town.totalSellingItemMap.size))
            val itemsToBuy = mutableListOf<ItemAmountLink>()
            for (i in 0..numItemTypesToBuy) {
                val randomItem = list[MathUtils.random(list.size - 1)] //Randomly pick an item
                itemsToBuy += ItemAmountLink(randomItem.first, MathUtils.random(1, 4)) //Get an item to buy
            }

            //Randomly assign an item and amount wanted
            val entity = Factory.createObjectFromJson("buyer", spawnPosition)
            val buying = Mappers.buyer[entity]
            val inventory = Mappers.inventory[entity]

            buying.buyList.addAll(com.badlogic.gdx.utils.Array(itemsToBuy.toTypedArray()))
            inventory.addItem("Gold", MathUtils.random(500, 1000))

            //Scan each item that we are buying and calculate necessity and luxury ratings
            buying.buyList.forEach { (itemName, itemAmount) ->
                val itemDef = DefinitionManager.itemDefMap[itemName]!!
                buying.needsSatisfactionRating += -itemDef.need
                buying.luxurySatisfactionRating += -itemDef.luxury
            }
        }
    }
}