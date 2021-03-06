package com.quickbite.economy.managers

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Json
import com.badlogic.gdx.utils.JsonValue
import com.moandjiezana.toml.Toml
import com.moandjiezana.toml.TomlWriter
import com.quickbite.economy.util.objects.ItemAmountLink
import com.quickbite.economy.util.objects.ItemPriceLink
import com.quickbite.economy.util.objects.TownItemIncome
import com.quickbite.economy.util.objects.WorkerTaskLimitLink

/**
 * Created by Paha on 2/16/2017.
 */
object DefinitionManager {
    private val json = Json()
    /** The definitions for all other definitions??? **/
    val definitionMap: HashMap<String, Definition> = hashMapOf()
    /** A map of all items, uncategorized **/
    val itemDefMap: HashMap<String, ItemDef> = hashMapOf()
    /** A map of the items broken down into category **/
    val itemCategoryMap:HashMap<String, com.badlogic.gdx.utils.Array<ItemDef>> = hashMapOf()
    /** The definitions for all item production chains **/
    val productionMap: HashMap<String, Production> = hashMapOf()
    /** The definitions for all plants **/
    val plantDefMap: HashMap<String, PlantDef> = hashMapOf()
    /** The definitions for all levels **/
    val levelDefMap:HashMap<String, LevelDef> = hashMapOf()
    /** The definitions for all constructions of buildings **/
    val constructionDefMap:HashMap<String, ConstructionDef> = hashMapOf()

    lateinit var names:Names

    private val buildingDefName = "data/buildingDefs.toml"
    private val unitDefName = "data/unitDefs.toml"
    private val resourceDefName = "data/resourceDefs.toml"
    private val itemsDefName = "data/itemDefs.toml"
    private val itemProdName = "data/productions.toml"
    private val namesDefName = "data/names.json"
    private val plantDefName = "data/plantDefs.toml"
    private val levelDefName = "data/levels.toml"
    private val constructionDefName = "data/constructionDef.toml"

    init {
        json.setSerializer(ItemPriceLink::class.java, object: Json.Serializer<ItemPriceLink> {
            override fun read(json: Json, jsonData: JsonValue, type: Class<*>): ItemPriceLink {
                val data = jsonData.child
                val itemAmountLink = ItemPriceLink(data.asString(), data.next.asInt()) //Make the item link
                return itemAmountLink //Return in
            }

            override fun write(json: Json, `object`: ItemPriceLink, knownType: Class<*>?) {
                json.writeValue(arrayOf(`object`.itemName, `object`.itemPrice)) //Write as an array
            }

        })

        json.setSerializer(ItemAmountLink::class.java, object: Json.Serializer<ItemAmountLink> {
            override fun read(json: Json, jsonData: JsonValue, type: Class<*>): ItemAmountLink {
                val data = jsonData.child
                val itemAmountLink = ItemAmountLink(data.asString(), data.next.asInt()) //Make the item link
                return itemAmountLink //Return in
            }

            override fun write(json: Json, `object`: ItemAmountLink, knownType: Class<*>?) {
                json.writeValue(arrayOf(`object`.itemName, `object`.itemAmount)) //Write as an array
            }

        })

        json.setSerializer(Vector2::class.java, object: Json.Serializer<Vector2> {
            override fun read(json: Json, jsonData: JsonValue, type: Class<*>): Vector2 {
                val data = jsonData.child
                return Vector2(data.asFloat(), data.next.asFloat()) //Return in
            }

            override fun write(json: Json, `object`: Vector2, knownType: Class<*>?) {
                json.writeValue(arrayOf(`object`.x, `object`.y)) //Write as an array
            }

        })

        //Add a serializer for ItemAmountLink
        json.setSerializer(ItemAmountLink::class.java, object: Json.Serializer<ItemAmountLink> {
            override fun read(json: Json, jsonData: JsonValue, type: Class<*>): ItemAmountLink {
                val data = jsonData.child
                return ItemAmountLink(data.asString(), data.next.asInt()) //Return in
            }

            override fun write(json: Json, `object`: ItemAmountLink, knownType: Class<*>?) {
                json.writeValue(arrayOf(`object`.itemName, `object`.itemAmount)) //Write as an array
            }
        })

        //Add a serializer for WorkerTaskLimitLink
        json.setSerializer(WorkerTaskLimitLink::class.java, object: Json.Serializer<WorkerTaskLimitLink> {
            override fun read(json: Json, jsonData: JsonValue, type: Class<*>): WorkerTaskLimitLink {
                val data = jsonData.child
                return WorkerTaskLimitLink(data.asString(), data.next.asInt()) //Return in
            }

            override fun write(json: Json, `object`: WorkerTaskLimitLink, knownType: Class<*>?) {
                json.writeValue(arrayOf(`object`.taskName, `object`.amount)) //Write as an array
            }
        })
    }

    fun loadDefinitions(){
        loadBuildingDefs()
        loadUnitDefs()
        loadResourceDefs()
        loadItemDefs()
        loadProductionDefs()
        loadPlantDefs()
        loadLevelDefs()
        loadConstructionDefs()
        this.names = json.fromJson(Names::class.java, Gdx.files.internal(namesDefName))
    }

    private fun loadBuildingDefs(){
        val buildingDefs = Toml().read(Gdx.files.internal(buildingDefName).file()).to(DefinitionManager.DefList::class.java)
        buildingDefs.defs.forEach { def -> DefinitionManager.definitionMap[def.name.toLowerCase()] = def
            def.type = "building" }
    }

    private fun loadUnitDefs(){
        val unitDefs = Toml().read(Gdx.files.internal(unitDefName).file()).to(DefinitionManager.DefList::class.java)
        unitDefs.defs.forEach { def -> DefinitionManager.definitionMap[def.name.toLowerCase()] = def
            def.type = "unit"}
    }

    private fun loadResourceDefs(){
        val resourceDefs = Toml().read(Gdx.files.internal(resourceDefName).file()).to(DefinitionManager.DefList::class.java)
        resourceDefs.defs.forEach { def -> DefinitionManager.definitionMap[def.name.toLowerCase()] = def
            def.type = "resource"}
    }

    private fun loadItemDefs(){
        val itemDefList = Toml().read(Gdx.files.internal(itemsDefName).file()).to(ItemDefList::class.java)
        itemDefList.items.forEach { itemDef ->
            DefinitionManager.itemDefMap[itemDef.itemName.toLowerCase()] = itemDef
            itemDef.categories.forEach { DefinitionManager.itemCategoryMap.computeIfAbsent(it, {com.badlogic.gdx.utils.Array()}).add(itemDef) }
        }
    }

    private fun loadProductionDefs(){
        val itemProdList = Toml().read(Gdx.files.internal(itemProdName).file()).to(DefinitionManager.ProductionList::class.java)
        itemProdList.productions.forEach { prod ->
            prod.produceItemName = prod.produceItemName.toLowerCase() //Make sure the produced item name is lower case
            prod.requirements.forEach { it.itemName = it.itemName.toLowerCase() } //Make sure each requirement item name is lower case
            DefinitionManager.productionMap[prod.produceItemName.toLowerCase()] = prod //Make sure we store it under the LOWER CASE name...
        }
    }

    private fun loadPlantDefs(){
        val plantDefsList = Toml().read(Gdx.files.internal(plantDefName).file()).to(PlantDefList::class.java)
        plantDefsList.defs.forEach { plantDef -> DefinitionManager.plantDefMap[plantDef.name] = plantDef }

        //TODO THis needs to be a class with a list of PlantDef objects!!
    }

    private fun loadLevelDefs(){
        val levelDefsList = Toml().read(Gdx.files.internal(levelDefName).file()).to(LevelDefList::class.java)
        levelDefsList.levels.forEach { level -> levelDefMap[level.name] = level }
    }

    private fun loadConstructionDefs(){
        val constructionDefList = Toml().read(Gdx.files.internal(constructionDefName).file()).to(ConstructionDefList::class.java)
        constructionDefList.defs.forEach { constr -> constructionDefMap[constr.name] = constr }
    }

    /**
     * Clears all definitions and reloads them
     */
    fun clearAllDataAndReload(){
        definitionMap.clear()
        itemDefMap.clear()
        itemCategoryMap.clear()
        productionMap.clear()

        loadDefinitions()
    }

    fun resaveDefinitions(){
        val buildingList = arrayListOf<Definition>()
        val resourceList = arrayListOf<Definition>()
        val unitList = arrayListOf<Definition>()

        definitionMap.values.forEach {
            when(it.type){
                "resource" -> resourceList += it
                "unit" -> unitList += it
                "building" -> buildingList += it
                else -> throw Exception("A defintion doesn't have a type. Fix me!")
            }
        }
        val buildingDefs = DefList().apply { defs = buildingList.toTypedArray() }

//        val writer = TomlWriter()

        val writer = TomlWriter.Builder()
                .indentValuesBy(2)
                .indentTablesBy(4)
                .build()

        writer.write(buildingDefs, Gdx.files.internal(buildingDefName).file())
    }

    class Names{
        lateinit var firstNames:com.badlogic.gdx.utils.Array<String>
        lateinit var lastNames:com.badlogic.gdx.utils.Array<String>
    }

    class DefList {
        lateinit var defs:Array<Definition>
    }

    class ConstructionDefList{
        var defs:Array<ConstructionDef> = arrayOf()
    }

    class ItemDefList{
        var items:Array<DefinitionManager.ItemDef> = arrayOf()
    }

    private class PlantDefList{
        lateinit var defs:Array<PlantDef>
    }

    class LevelDefList{
        lateinit var levels:Array<LevelDef>
    }

    class LevelDef{
        lateinit var name:String
        var startingMoney:Float = 0f
        var buildings:Array<LevelBuildingDef> = arrayOf()
        lateinit var townDef:LevelTownDef
    }

    class LevelBuildingDef{
        lateinit var buildingName:String
        lateinit var position:Vector2
        var workers:Array<Array<String>> = arrayOf()
        var importing:Array<String> = arrayOf()
        var items:Array<ItemAmountLink> = arrayOf()
    }

    class LevelTownDef{
        var startingPop = 200f
        var imports = arrayOf<TownItemIncome>()
    }

    class PlantDef{
        val name = ""
        var displayName = ""
        var graphicName = ""
        var timeToGrow = 0
        var harvestAmount = 0
        var chanceForTend = 0f

        override fun toString(): String {
            return name
        }
    }

    class Definition {
        var name = ""
        var type = ""
        var identityDef = IdentityDef() //Must exist
        var graphicDef = GraphicDef() //Must exist
        var transformDef = TransformDef() //Must exist
        var gridDef:GridDef = GridDef() //Must exist
        var buildingDef:BuildingDef? = null //Optional
        var velocityDef:VelocityDef? = null //Optional
        var physicsDef:PhysicsDef? = null //Optional
        var inventoryDef:InventoryDef? = null //Optional
        var productionDef:ProductionDef? = null //Optional
        var sellingItems:SellingDef? = null //Optional
        var workforceDef:WorkforceDef? = null //Optional
        var resourceDef:ResourceDef? = null //Optional
        var farmDef:FarmDef? = null //Optional
        var behaviourDef:BehaviourDef? = null //Optional
        var workerDef:WorkerDef? = null //Optional
        var buyerDef:BuyerDef? = null //Optional
        var compsToAdd:List<ComponentDef> = listOf()
    }

    class IdentityDef{
        var useRandomName:Boolean = false
    }

    class GridDef{
        var onGrid = false
        var blockGridWhenPlaced = false
        var gridSpotsToBlock = arrayOf<Array<Int>>()
    }

    class ProductionDef{
        var produces:Array<String> = arrayOf()
        var harvests:Array<String> = arrayOf()
    }

    class BehaviourDef

    class WorkerDef

    class BuyerDef

    class TransformDef{
        var physicalDimensions = Vector2()
        var spots:HashMap<String, Array<Vector2>> = hashMapOf()
    }

    class WorkforceDef{
        var workforceMax = 0
        lateinit var workerTasks:Array<WorkerTaskLimitLink>
    }

    class BuildingDef{
        var isBuilding = false
        var buildingType = ""
    }

    class PhysicsDef{
        var bodyType = ""
        var bodyAnchor = Vector2()
        var bodyDimensions = Vector2()
    }

    class VelocityDef{
        var baseSpeed = 0f
    }

    class InventoryDef{
        var debugItemList:Array<ItemAmountLink> = arrayOf()
        var outputs = arrayOf<String>()
    }

    class ComponentDef{
        var compName = ""
        var fields:Array<Array<String>> = arrayOf()
    }

    class GraphicDef{
        var graphicName = ""
        var graphicAnchor:Array<Float> = arrayOf()
        var graphicSize:Array<Float> = arrayOf()
        var initialAnimation = true
    }

    class SellingDef{
        var sellingList:Array<ItemPriceLink> = arrayOf()
        var isReselling = false
        var taxRate = 0f
    }

    class ResourceDef{
        var resourceType:String = ""
        var resourceAmount:Int = 0
        var numHarvestersMax:Int = 0
        var harvestAmount = 0
        var harvestedItemName = ""
        var baseHarvestTime = 0f
        var canRegrow = false
        var baseRegrowTimeRange = arrayOf(180, 300)
        var harvestedGraphicName = ""
    }

    class FarmDef{
        var xSpace:Float = 0f
        var ySpace:Float = 0f
        var rows:Int = 0
        var cols:Int = 0
        var offset:Vector2 = Vector2()
    }

    class ItemDef{
        lateinit var itemName:String
        var baseMarketPrice:Int = 0
        var categories:Array<String> = arrayOf()
        var iconName = ""
        var need = 0
        var luxury = 0
        var baseQuality = 1
    }

    class ProductionList{
        lateinit var productions:Array<Production>
    }

    class Production{
        lateinit var produceItemName:String
        var produceAmount:Int = 0
        lateinit var requirements:Array<ItemAmountLink>
    }

    class ConstructionDef{
        lateinit var name:String
        var cost = 0
        var time = 60 //Default of 1 hour
        var materials = arrayOf<ItemAmountLink>() //List of item names
    }
}