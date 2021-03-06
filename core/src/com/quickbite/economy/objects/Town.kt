package com.quickbite.economy.objects

import com.badlogic.gdx.math.MathUtils
import com.quickbite.economy.event.GameEventSystem
import com.quickbite.economy.event.events.PopulationChangeEvent
import com.quickbite.economy.util.CircularQueueWrapper
import com.quickbite.economy.util.CustomTimer
import com.quickbite.economy.util.TimeOfDay
import com.quickbite.economy.util.objects.TownItemIncome

/**
 * Created by Paha on 3/18/2017.
 */
class Town(val name:String) {
    var population:Float = 0f
    var money = 0f

    val populationHistory = CircularQueueWrapper<Int>(50)
    val itemImportMap = hashMapOf<String, TownItemIncome>()
    val totalSellingItemMap = hashMapOf<String, Int>()
    val populationIncreaseFromRatingThreshold = 20

    var needsRating:Int = 500
        set(value){
            field = MathUtils.clamp(value, 0, 1000)
        }

    var luxuryRating:Int = 500
        set(value){
            field = MathUtils.clamp(value, 0, 1000)
        }

    private val accumulateItemsTimer:CustomTimer
    private val changePopulationTimer:CustomTimer

    init{
        //TODO Make sure this timer works
        accumulateItemsTimer = CustomTimer(0f, 1f, false, {
            //For each item in the income map, increase it!
            itemImportMap.values.forEach { income ->
                //1440 is how many minutes in 24 hours
                income.accumulatedItemCounter = income.accumulatedItemCounter + (income.calculatedProductionAmtPerDay/(1440f/(TimeOfDay.currScaledTime)))
            }
        })

        changePopulationTimer = CustomTimer(0f, 10f, false, {
            val needDiff = needsRating - 500 //This will either be negative or positive
            val needsChange = needDiff.toFloat()/populationIncreaseFromRatingThreshold.toFloat() //Negative or positive change
            val luxuryDiff = luxuryRating - 500 //This will either be negative or positive
            val luxuryChange = luxuryDiff.toFloat()/populationIncreaseFromRatingThreshold.toFloat() //Negative or positive change

            //If needs is below 500, remove from needs. Otherwise, add the luxury amount.
            val change:Float = if(needDiff > 0) Math.max(0f, luxuryChange) else needsChange

            population = Math.max(0f, population + change)
            populationHistory += population.toInt() //Add the most recent population to the history
            GameEventSystem.fire(PopulationChangeEvent(population.toInt(), populationHistory.queue.toList()))
        })
    }

    fun update(delta:Float){
        accumulateItemsTimer.update(delta)
        changePopulationTimer.update(delta)
    }

    fun getPopulation():Int{
        return population.toInt()
    }
}