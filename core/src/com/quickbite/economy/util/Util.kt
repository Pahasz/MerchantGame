package com.quickbite.economy.util

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.FixtureDef
import com.badlogic.gdx.physics.box2d.PolygonShape
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.quickbite.economy.MyGame
import com.quickbite.economy.components.BuildingComponent
import com.quickbite.economy.components.ResourceComponent
import com.quickbite.economy.components.SellingItemsComponent
import com.quickbite.economy.managers.DefinitionManager
import com.quickbite.economy.managers.TownManager
import com.quickbite.economy.util.objects.SellingItemData
import com.quickbite.economy.util.objects.TownItemIncome


/**
 * Created by Paha on 1/16/2017.
 */
object Util {
    fun createPixel(color: Color): Texture {
        return createPixel(color, 1, 1)
    }

    fun createPixel(color: Color, width: Int, height: Int): Texture {
        val pixmap = Pixmap(width, height, Pixmap.Format.RGBA8888)
        pixmap.setColor(color.r, color.g, color.b, color.a)
        pixmap.fillRectangle(0, 0, width, height)
        val pixmaptex = Texture(pixmap)
        pixmap.dispose()

        return pixmaptex
    }

    fun roundUp(a:Float, increment:Int):Int{
        return (Math.ceil(a.toDouble()/increment)*increment).toInt()
    }

    fun roundDown(a:Float, increment:Int):Int{
        return (Math.floor(a.toDouble()/increment)*increment).toInt()
    }

    fun createBody(bodyType: BodyDef.BodyType, dimensions:Vector2, initialPosition:Vector2, fixtureData:Any, isSensor:Boolean = false): Body {
        val bodyDef = BodyDef()
        bodyDef.type = bodyType
        bodyDef.position.set(initialPosition)
        val body = MyGame.world.createBody(bodyDef)

        val fixtureDef = FixtureDef()
        val boxShape = PolygonShape()
        boxShape.setAsBox(dimensions.x*0.5f, dimensions.y*0.5f)

        fixtureDef.shape = boxShape
        fixtureDef.isSensor = isSensor

        val fixture = body.createFixture(fixtureDef)
        fixture.userData = fixtureData

        boxShape.dispose()

        return body
    }

    fun createBody(bodyType: String, dimensions:Vector2, initialPosition:Vector2, fixtureData:Any, isSensor:Boolean = false): Body {
        val _bodyType:BodyDef.BodyType
        when(bodyType){
            "dynamic" -> _bodyType = BodyDef.BodyType.DynamicBody
            else -> _bodyType = BodyDef.BodyType.StaticBody
        }

        return createBody(_bodyType, dimensions, initialPosition, fixtureData, isSensor)
    }

    fun getBuildingType(type:String):BuildingComponent.BuildingType{
        when(type.toLowerCase()){
            "wall" -> return BuildingComponent.BuildingType.Wall
            "shop" -> return BuildingComponent.BuildingType.Shop
            "workshop" -> return BuildingComponent.BuildingType.Workshop
            "stockpile" -> return BuildingComponent.BuildingType.Stockpile
            "house" -> return BuildingComponent.BuildingType.House
            else -> return BuildingComponent.BuildingType.None
        }
    }

    fun drawLineTo(start: Vector2, end:Vector2, pixel:TextureRegion, size:Float, batch: Batch){
        val rotation = MathUtils.atan2(end.y - start.y, end.x - start.x)* MathUtils.radiansToDegrees
        val distance = start.dst(end)
        pixel.setRegion(0f, 0f, distance/ size, 1f)
        batch.draw(pixel, start.x, start.y, 0f, 0f, distance, size, 1f, 1f, rotation)
    }

    fun drawLineTo(start: Vector2, end:Vector2, pixel: TextureRegionDrawable, size:Float, batch: Batch){
        val rotation = MathUtils.atan2(end.y - start.y, end.x - start.x)* MathUtils.radiansToDegrees
        val distance = start.dst(end)
        pixel.draw(batch, start.x, start.y, 0f, 0f, distance, size, 1f, 1f, rotation)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> toObject(clazz: Class<*>, value: String): T {
        if (Boolean::class.java == clazz) return java.lang.Boolean.parseBoolean(value) as T
        if (Byte::class.java == clazz) return java.lang.Byte.parseByte(value) as T
        if (Short::class.java == clazz) return java.lang.Short.parseShort(value) as T
        if (Int::class.java == clazz) return Integer.parseInt(value) as T
        if (Long::class.java == clazz) return java.lang.Long.parseLong(value) as T
        if (Float::class.java == clazz) return java.lang.Float.parseFloat(value) as T
        if (Double::class.java == clazz) return java.lang.Double.parseDouble(value) as T
        return value as T
    }

    fun changeRoadOnTerrain(node:Grid.GridNode, roadLevel:Int){
        node.terrain!!.roadLevel = roadLevel
    }

    /**
     * Removes an item from the reselling list of the selling component. If there was an item to remove, the linked entity that
     * was selling the item is given back the selling capabilities
     * @param sellingComp The SellingItemsComponent to modify
     * @param itemName The name of the item
     */
    fun removeSellingItemFromReseller(sellingComp:SellingItemsComponent, itemName:String, itemSourceType: SellingItemData.ItemSource, itemSourceData:Any? = null){
//        sellingComp.currSellingItems.removeAll { it.itemName == itemName && it.itemSourceType == itemSourceType
//                && it.itemSourceData == itemSourceData } //Remove all currently selling items with this name

        //Deal with the source type
        when(itemSourceType){
            //If the source is from a workshop, we need to remove it from us and give it back to the workshop
            SellingItemData.ItemSource.Workshop -> {
                val otherSelling = Mappers.selling[itemSourceData as Entity] //Get the selling component of the linked Entity
                val baseSellingItem = otherSelling?.baseSellingItems?.firstOrNull { it.itemName == itemName } //Get the base selling item
                //If the base selling item is not null AND the linked Entity is not already currently selling it
                if (baseSellingItem != null && !otherSelling.currSellingItems.any { it.itemName == itemName })
                    otherSelling.currSellingItems.add(baseSellingItem.copy()) //Add it back into the current selling list

                sellingComp.currSellingItems.removeAll { it.itemName == itemName && it.itemSourceType == SellingItemData.ItemSource.Workshop
                    && it.itemSourceData as Entity == itemSourceData}
            }

            //If it's an import from a town...
            SellingItemData.ItemSource.Import ->{
                //TODO We need to figure out how to get the correct town here. This is just a prototyping quickie here
                //Get the first item that matches the name AND the item source data passed in
                val item = sellingComp.currSellingItems.first{it.itemName == itemName && it.itemSourceData == itemSourceData}
                //Get the town using the item source data
                TownManager.getTown(item.itemSourceData as String).itemImportMap[itemName]!!.linkedToEntity = null
                //Remove the item from the selling comp
                sellingComp.currSellingItems.removeAll { it.itemName == itemName && it.itemSourceData == itemSourceData}
            }
            SellingItemData.ItemSource.Myself ->{
                sellingComp.currSellingItems.removeAll { it.itemName == itemName && it.itemSourceData == itemSourceData}
            }
            else -> {

            }
        }

        //Remove all matching items from the reselling list. Make sure the itemSourceData is not null. This is
        //important because we only want to remove links to other buildings/sellers
    }

    /**
     * Attemps to assign an Entity worker to an Entity with a WorkForce. Fails if the workforce can't accept any more workers
     * @param entityWorker The Entity that has a WorkerComponent that will be added to the workforce
     * @param entityWorkForce The Entity that has a WorkForceComponent that will be managing the worker Entity.
     * @return True if a worker was able to be added, false otherwise
     */
    fun assignWorkerToBuilding(entityWorker:Entity, entityWorkForce:Entity):Boolean{
        val worker = Mappers.worker[entityWorker]
        val workForce = Mappers.workforce[entityWorkForce]
        if(workForce.workers.size >= workForce.numWorkerSpots) //If we're at our max, return false
            return false

        workForce.workers.add(entityWorker)
        worker.workerBuilding = entityWorkForce

        if(workForce.workerTasksLimits.size == 1)
            toggleTaskOnWorker(entityWorker, entityWorkForce, workForce.workerTasksLimits[0].taskName)

        return true
    }

    fun createAndAssignWorkerToBuilding(entityWorkForce:Entity):Entity?{
        val entity = Factory.createObjectFromJson("worker", Vector2(-1000f, 0f))!!
        return if(assignWorkerToBuilding(entity, entityWorkForce)) entity else null
    }

    /**
     * Adds or removes a task from a worker
     * @param workerEntity The worker Entity to assign the task to
     * @param workforceEntity The workforce the Entity is assigned to. This is needed to pull work limits from
     * @param taskName The name of the task to add
     */
    fun toggleTaskOnWorker(workerEntity:Entity, workforceEntity : Entity, vararg taskNames:String){
        val worker = Mappers.worker[workerEntity]
        val workforce = Mappers.workforce[workforceEntity]

        taskNames.forEach { taskName ->
            val workerTaskLimitLink = workforce.workerTasksLimits.find { it.taskName == taskName }!!

            when (worker.taskList.contains(taskName)) {
                true -> { //If it does contain it, remove it!
                    worker.taskList.removeValue(taskName, false)
                    workforce.workerTaskMap[taskName]!!.removeValue(workerEntity, true)
                }
                else -> { //If it doesn't contain it, add it!
                    //Make sure we have enough room to add it
                    if (workforce.workerTaskMap[taskName]!!.size < workerTaskLimitLink.amount) {
                        worker.taskList.add(taskName)
                        workforce.workerTaskMap[taskName]!!.add(workerEntity)
                    }
                }
            }
        }
    }

    /**
     * Adds an item to the reselling list of an Entity that does reselling.
     * @param resellingEntity The Entity that is reselling the item
     * @param itemName The name of the item
     * @param itemSource The source of the item (import, town, etc)
     * @param The source data (ie: where it came from, entity?)
     */
    fun addItemToEntityReselling(resellingEntity:Entity, itemName: String, itemSource: SellingItemData.ItemSource, sourceData:Any? = null){
        val itemName = itemName.toLowerCase()
        val selling = Mappers.selling[resellingEntity]

        val itemDef = DefinitionManager.itemDefMap[itemName]!!
        val sellingData = SellingItemData(itemDef.itemName, (itemDef.baseMarketPrice * 1.5f).toInt(), -1, itemSource, sourceData)

        //If the reselling and currSelling lists don't already contain this...
        if(!selling.currSellingItems.any { it.itemName == itemName && it.itemSourceData == sourceData} &&
                !selling.currSellingItems.any { it.itemName == itemName && it.itemSourceData == sourceData}) {

            selling.currSellingItems.add(sellingData)
        }
    }

    fun addImportItemToEntityReselling(itemIncome: TownItemIncome, resellingEntity:Entity, sourceData:Any? = null){
        itemIncome.linkedToEntity = resellingEntity
        addItemToEntityReselling(resellingEntity, itemIncome.itemName, SellingItemData.ItemSource.Import, sourceData)
    }

    fun removeWorkerFromBuilding(entityWorker:Entity, entityWorkForce: Entity){

    }

    /**
     * Sets a resource as harvested. Handles if the resource can regrow or should be destroyed
     * @param entity The entity that has the resource component
     * @param rc Optional resource component object if convenient
     */
    fun setResourceAsHarvested(entity:Entity, rc:ResourceComponent? = null){
        val rc = rc ?: Mappers.resource[entity]

        if(!rc.canRegrow){
            Factory.destroyEntity(entity)
        }else{
            rc.harvested = true
            Mappers.graphic[entity].sprite.setRegion(MyGame.manager[rc.harvestedGraphicName, Texture::class.java])
            rc.nextRegrowTime = MathUtils.random(rc.baseRegrowTime[0], rc.baseRegrowTime[1]).toFloat() //Might as well set our next regrow time
        }
    }

}