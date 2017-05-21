package com.quickbite.economy.gui

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Array
import com.quickbite.economy.MyGame
import com.quickbite.economy.addChangeListener
import com.quickbite.economy.components.*
import com.quickbite.economy.event.GameEventSystem
import com.quickbite.economy.event.events.ItemSoldEvent
import com.quickbite.economy.event.events.ReloadGUIEvent
import com.quickbite.economy.gui.widgets.Graph
import com.quickbite.economy.interfaces.GUIWindow
import com.quickbite.economy.objects.SelectedWorkerAndTable
import com.quickbite.economy.objects.SellingItemData
import com.quickbite.economy.util.Factory
import com.quickbite.economy.util.GUIUtil
import com.quickbite.economy.util.Mappers
import com.quickbite.economy.util.Util

/**
 * Created by Paha on 3/9/2017.
 */
class EntityWindow(guiManager: GameScreenGUIManager, val entity:Entity) : GUIWindow(guiManager){

    private var currentlyDisplayingComponent: Component? = null
    private var currentlySelectedEntity: Entity? = null
    private var currentTabType: Int = 0
    private var selectedWorkers = Array<SelectedWorkerAndTable>()

    private var lastWorkerListCounter = 0

    init {
        val identity = Mappers.identity[entity]
        window.titleLabel.setText(identity.name)
        initEntityStuff()
    }

    private fun initEntityStuff(){
        currentlySelectedEntity = entity

        val sc = Mappers.selling.get(entity)
        val wc = Mappers.workforce.get(entity)
        val bc = Mappers.building.get(entity)
        val behComp = Mappers.behaviour.get(entity)
        val ic = Mappers.inventory.get(entity)
        val buying = Mappers.buyer.get(entity)

        val buildingTab = TextButton("Building", defaultTextButtonStyle)
        buildingTab.label.setFontScale(1f)

        val sellTab = TextButton("Sell", defaultTextButtonStyle)
        sellTab.label.setFontScale(1f)

        val economyTab = TextButton("Eco", defaultTextButtonStyle)
        economyTab.label.setFontScale(1f)

        val workTab = TextButton("Work", defaultTextButtonStyle)
        workTab.label.setFontScale(1f)

        val behaviourTab = TextButton("Beh", defaultTextButtonStyle)
        behaviourTab.label.setFontScale(1f)

        val inventoryTab = TextButton("Inv", defaultTextButtonStyle)
        inventoryTab.label.setFontScale(1f)

        val buyingLabel = TextButton("Buying", defaultTextButtonStyle)
        buyingLabel.label.setFontScale(1f)

        val deleteLabel = TextButton("Delete", defaultTextButtonStyle)
        deleteLabel.label.setFontScale(1f)

        val exitLabel = TextButton("X", defaultTextButtonStyle)
        exitLabel.label.setFontScale(1f)

        if(bc != null)
            tabTable.add(buildingTab).spaceRight(5f)
        if(sc != null) {
            tabTable.add(sellTab).spaceRight(5f)
            tabTable.add(economyTab).spaceRight(5f)
        }
        if(wc != null)
            tabTable.add(workTab).spaceRight(5f)
        if(behComp != null)
            tabTable.add(behaviourTab).spaceRight(5f)
        if(ic != null)
            tabTable.add(inventoryTab).spaceRight(5f)
        if(buying != null)
            tabTable.add(buyingLabel).spaceRight(5f)

        tabTable.add(deleteLabel)

        tabTable.add().growX()
        tabTable.add(exitLabel).right().width(32f)

        buildingTab.addListener(object: ClickListener(){
            override fun touchUp(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int) {
                super.touchUp(event, x, y, pointer, button)
                loadTable(contentTable, bc, 1)
            }
        })

        sellTab.addListener(object: ClickListener(){
            override fun touchUp(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int) {
                super.touchUp(event, x, y, pointer, button)
                loadTable(contentTable, sc, 2)
            }
        })

        economyTab.addListener(object: ClickListener(){
            override fun touchUp(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int) {
                super.touchUp(event, x, y, pointer, button)
                loadTable(contentTable, sc, 3)
            }
        })


        workTab.addListener(object: ClickListener(){
            override fun touchUp(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int) {
                super.touchUp(event, x, y, pointer, button)
                loadTable(contentTable, wc, 4)
            }
        })

        behaviourTab.addListener(object: ClickListener(){
            override fun touchUp(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int) {
                super.touchUp(event, x, y, pointer, button)
                loadTable(contentTable, behComp, 5)
            }
        })

        inventoryTab.addListener(object: ClickListener(){
            override fun touchUp(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int) {
                super.touchUp(event, x, y, pointer, button)
                loadTable(contentTable, ic, 6)
            }
        })

        buyingLabel.addListener(object: ClickListener(){
            override fun touchUp(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int) {
                super.touchUp(event, x, y, pointer, button)
                loadTable(contentTable, buying, 7)
            }
        })

        exitLabel.addListener(object: ClickListener(){
            override fun touchUp(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int){
                super.touchUp(event, x, y, pointer, button)
                close()
            }
        })

        deleteLabel.addListener(object:ClickListener(){
            override fun touchUp(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int) {
                close()
                Factory.destroyEntity(entity)
            }
        })

        //When we select a new building, try to display whatever section we were displaying on the last one
        if(currentlyDisplayingComponent != null)
            loadTable(contentTable, currentlyDisplayingComponent!!, currentTabType)

//        window.debugAll()
    }

    override fun update(delta:Float){
        updateFuncsList.forEach { it() }
    }

    override fun close() {
        super.close()

        //Prevent this crash....
        if(currentlySelectedEntity != null) {
            val debug = Mappers.debugDraw[currentlySelectedEntity]
            debug.debugDrawWorkers = false
            debug.debugDrawWorkplace = false
        }

        currentlySelectedEntity = null
    }

    private fun loadTable(table: Table, component: Component, tabType:Int){
        table.clear()
        updateFuncsList.clear()

        val debug = Mappers.debugDraw[currentlySelectedEntity]
        debug.debugDrawWorkers = false
        debug.debugDrawWorkplace = false

        changedTabsFunc() //Call the change tabs function
        changedTabsFunc = {} //Clear the change tabs function

        when(tabType){
            1 -> setupBuildingTable(table, component as BuildingComponent)
            2 -> setupSellingTable(table, component as SellingItemsComponent)
            3 -> setupEconomyTable(table, component as SellingItemsComponent)
            4 -> {
                debug.debugDrawWorkers = true
                setupWorkforceTable(table, component as WorkForceComponent)
            }
            5 -> setupBehaviourTable(table, component as BehaviourComponent)
            6-> setupInventoryTable(table, component as InventoryComponent)
            7 -> setupBuyingTable(table, component as BuyerComponent)
        }

        currentlyDisplayingComponent = component
        currentTabType = tabType
    }

    private fun setupBuildingTable(table: Table, comp: BuildingComponent){
        val typeLabel = Label("Type: ${comp.buildingType}", defaultLabelStyle)
        typeLabel.setFontScale(1f)
        val queueLabel = Label("Queue size: ${comp.unitQueue.size}", defaultLabelStyle)
        queueLabel.setFontScale(1f)

        table.add(typeLabel).expandX().fillX()
        table.row()
        table.add(queueLabel).expandX().fillX()

        updateFuncsList.add({typeLabel.setText("Type: ${comp.buildingType}")})
        updateFuncsList.add({queueLabel.setText("Queue size: ${comp.unitQueue.size}")})
    }

    private fun setupWorkforceTable(table: Table, comp: WorkForceComponent){
        val scrollPaneStyle = ScrollPane.ScrollPaneStyle()
        scrollPaneStyle.background = darkBackgroundDrawable
        scrollPaneStyle.vScroll = TextureRegionDrawable(TextureRegion(Util.createPixel(Color.WHITE)))
        scrollPaneStyle.vScrollKnob = TextureRegionDrawable(TextureRegion(Util.createPixel(Color(Color.BLACK))))
        scrollPaneStyle.hScroll = TextureRegionDrawable(TextureRegion(Util.createPixel(Color.WHITE)))
        scrollPaneStyle.hScrollKnob = TextureRegionDrawable(TextureRegion(Util.createPixel(Color(Color.BLACK))))

        val buttonStyle = Button.ButtonStyle()
        buttonStyle.up = buttonBackgroundDrawable

        //Our scroll pane
        val workerListTable = Table() //Our worker list
        val mainScrollPane = ScrollPane(workerListTable, scrollPaneStyle)

        workerListTable.top()

        fun populateWorkerTable() {
            workerListTable.clear()

            val nameTitleLabel = Label("Name", defaultLabelStyle)
            nameTitleLabel.setAlignment(Align.center)
            nameTitleLabel.setFontScale(1.2f)
            val currTaskTitleLabel = Label("Tasks", defaultLabelStyle)
            currTaskTitleLabel.setAlignment(Align.center)
            currTaskTitleLabel.setFontScale(1.2f)
            val startTimeTitleLAbel = Label("Start", defaultLabelStyle)
            startTimeTitleLAbel.setAlignment(Align.center)
            startTimeTitleLAbel.setFontScale(1.2f)
            val endTimeTitleLabel = Label("End", defaultLabelStyle)
            endTimeTitleLabel.setAlignment(Align.center)
            endTimeTitleLabel.setFontScale(1.2f)
            val salaryTitleLabel = Label("Salary", defaultLabelStyle)
            salaryTitleLabel.setAlignment(Align.center)
            salaryTitleLabel.setFontScale(1.2f)
            val fireTitleLabel = Label("Fire", defaultLabelStyle)
            fireTitleLabel.setAlignment(Align.center)
            fireTitleLabel.setFontScale(1.2f)

            val titleTable = Table()
            workerListTable.add(titleTable).growX()
            workerListTable.row().growX()

            //Add all the titles....
            titleTable.add(nameTitleLabel).growX().uniformX()
            titleTable.add(currTaskTitleLabel).growX().uniformX()
            titleTable.add(startTimeTitleLAbel).growX().uniformX()
            titleTable.add(endTimeTitleLabel).growX().uniformX()
            titleTable.add(salaryTitleLabel).growX().uniformX()
            titleTable.add(fireTitleLabel).growX().uniformX()

            comp.workersAvailable.forEach { entity ->
                val workerTable = GUIUtil.makeWorkerTable(entity, comp, defaultLabelStyle, defaultTextButtonStyle)

                workerListTable.add(workerTable).growX()
                workerListTable.row().growX()

                //Since this table gets updated on changes, make sure to keep the background if we reload this table
                //Also, we check the entity because the tables are new and won't match. We need to set the table again
                selectedWorkers.firstOrNull { it.worker == entity }?.run {
                    workerTable.background = TextureRegionDrawable(TextureRegion(Util.createPixel(Color.GRAY))) //Set the background of the selected table
                    this.table = workerTable
                }

                workerTable.addListener(object:ClickListener(){
                    override fun touchUp(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int) {
                        super.touchUp(event, x, y, pointer, button)
                        val holdingShift = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)
                        var existingWorker:SelectedWorkerAndTable? = null

                        //If we're not holding shift, clear the list before starting again
                        if(!holdingShift) {
                            selectedWorkers.forEach { it.table.background = null } //Clear the background of each thing
                            selectedWorkers.clear() //Clear the list

                        //If we're holding shift, we need to check if the worker is already selected. If so, unselect it!
                        }else{
                            existingWorker = selectedWorkers.firstOrNull { it.worker == entity }
                            if(existingWorker != null) { //If not null, unselect it
                                existingWorker.table.background = null
                                selectedWorkers.removeValue(existingWorker, true)

                            //Otherwise, add the new one
                            }
                        }

                        if(existingWorker == null) {
                            selectedWorkers.add(SelectedWorkerAndTable(entity, workerTable))
                            workerTable.background = TextureRegionDrawable(TextureRegion(Util.createPixel(Color.GRAY))) //Set the background of the selected table
                        }
                    }
                })
            }
        }

        populateWorkerTable()

        val workerTaskList = Table()

        /** This sets the tasks that are available to set to each worker */
        comp.workerTasks.forEach { taskName ->
            val taskNameLabel = Label(taskName, defaultLabelStyle)
            taskNameLabel.setFontScale(1f)

            workerTaskList.add(taskNameLabel).space(0f, 5f, 0f, 5f)

            //When we click the task name label: if it's not owned by the worker, remove it. Otherwise, add it
            taskNameLabel.addListener(object:ClickListener(){
                override fun clicked(event: InputEvent?, x: Float, y: Float) {
                    super.clicked(event, x, y)

                    selectedWorkers.forEach { selected ->
                        val taskNameText = taskNameLabel.text.toString()
                        val worker = Mappers.worker[selected.worker]!!

                        //If it doesn't contain it, add it. Otherwise, remove it
                        if(!worker.taskList.contains(taskNameText))
                            worker.taskList.add(taskNameText)
                        else
                            worker.taskList.removeValue(taskNameText, false)
                    }

                    populateWorkerTable()
                }
            })
        }

        //The button to open the hire window
        val hireButton = TextButton("Hire", defaultTextButtonStyle)
        hireButton.label.setFontScale(1f)

        //Hire button listener
        hireButton.addListener(object:ChangeListener(){
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                guiManager.openHireWindow(this@EntityWindow.entity)
            }
        })

        //An event to listen for a worker to be hired
        val updateEvent = GameEventSystem.subscribe<ReloadGUIEvent> { populateWorkerTable() }

        //Remember to remove this from the event system
        changedTabsFunc = { GameEventSystem.unsubscribe(updateEvent) }

        //Add our main scroll pane and the hiring button
        table.add(mainScrollPane).grow().top().colspan(2).height(250f)
        table.row()
        table.add(workerTaskList)
        table.add(hireButton)
    }

    private fun setupBehaviourTable(table: Table, comp: BehaviourComponent){
        val taskLabel = Label("CurrTask: ${comp.currTask}", defaultLabelStyle)
        taskLabel.setFontScale(1f)
        taskLabel.setWrap(true)

        table.add(taskLabel).expandX().fillX()

        updateFuncsList.add({taskLabel.setText("CurrTask: ${comp.currTask}")})
    }

    private fun setupInventoryTable(table: Table, comp: InventoryComponent){

        val contentsTable = Table()
        contentsTable.background = darkBackgroundDrawable

        val amountLabel = Label("Amount of items: ${comp.itemMap.size}", defaultLabelStyle)
        amountLabel.setFontScale(1f)

        val listLabel = Label("Item List", defaultLabelStyle)
        listLabel.setFontScale(1f)
        listLabel.setAlignment(Align.center)

        //The main table...
        table.add(amountLabel).expandX().fillX()
        table.row()
        table.add(contentsTable).expandX().fillX()
        table.row()

        val populateItemTable = {
            contentsTable.clear()
            //The contents table
            contentsTable.add(listLabel).colspan(2)
            contentsTable.row()

            comp.itemMap.values.forEach { (itemName, itemAmount) ->
                val itemLabel = Label(itemName, defaultLabelStyle)
                itemLabel.setFontScale(1f)
                itemLabel.setAlignment(Align.center)

                val itemAmountLabel = Label("$itemAmount", defaultLabelStyle)
                itemAmountLabel.setFontScale(1f)
                itemAmountLabel.setAlignment(Align.center)

                contentsTable.add(itemLabel).width(100f)
                contentsTable.add(itemAmountLabel).width(100f)
                contentsTable.row()
            }
        }

        populateItemTable()

        table.top()

        updateFuncsList.add({populateItemTable()})
//        updateFuncsList.add({listLabel.setText("Item List: ${comp.itemMap.values}")})
    }

    private fun setupBuyingTable(table: Table, comp: BuyerComponent){
        val buyingTable = Table()

        val itemNameTitle = Label("Item", defaultLabelStyle)
        itemNameTitle.setFontScale(1f)

        val itemAmountTitle = Label("Amount", defaultLabelStyle)
        itemAmountTitle.setFontScale(1f)

        val necessityRatingLabel = Label("Needs: ${comp.needsSatisfactionRating}", defaultLabelStyle)
        necessityRatingLabel.setFontScale(1f)

        val luxuryRatingLabel = Label("Luxury: ${comp.luxurySatisfactionRating}", defaultLabelStyle)
        luxuryRatingLabel.setFontScale(1f)

        val populateTableFunc = {
            buyingTable.clear()

            luxuryRatingLabel.setText("Luxury: ${comp.luxurySatisfactionRating}")
            necessityRatingLabel.setText("Needs: ${comp.needsSatisfactionRating}")

            buyingTable.add(necessityRatingLabel)
            buyingTable.add(luxuryRatingLabel)
            buyingTable.row()
            buyingTable.add(itemNameTitle)
            buyingTable.add(itemAmountTitle)

            comp.buyList.forEach { pair ->
                val itemName = Label(pair.itemName, defaultLabelStyle)
                itemName.setFontScale(1f)
                val itemAmount = Label("${pair.itemAmount}", defaultLabelStyle)
                itemAmount.setFontScale(1f)

                buyingTable.row()
                buyingTable.add(itemName)
                buyingTable.add(itemAmount)
            }
        }

        populateTableFunc()

        updateFuncsList.add {  populateTableFunc() }

        table.add(buyingTable)

//        updateFuncsList.add(UpdateLabel(nameLabel, { label -> label.setText("CurrTaskName: ${comp.currTaskName}")}))
    }

    private fun setupSellingTable(table: Table, comp: SellingItemsComponent){
        contentTable.debugAll()
        val taxRateTable = Table()
        taxRateTable.background = darkBackgroundDrawable

        val sellItemsMainTable = Table()
        sellItemsMainTable.background = darkBackgroundDrawable
        sellItemsMainTable.debugAll()

        val sellHistoryTable = Table()
        sellHistoryTable.background = darkBackgroundDrawable

        //Set up the selling title
        val sellLabel = Label("Selling", defaultLabelStyle)
        sellLabel.setAlignment(Align.center)
        sellLabel.setFontScale(1.2f)

        //This will populate the table of items being sold
        val populateItemsTable = {
            sellItemsMainTable.clear()

            sellItemsMainTable.add(sellLabel)
            sellItemsMainTable.row()

            val sellItemsListTable = Table()
            sellItemsListTable.debugAll()

            val itemNameColTitle = Label("Name", defaultLabelStyle)
            itemNameColTitle.setFontScale(1f)
            itemNameColTitle.setAlignment(Align.center)

            val itemAmountColTitle = Label("Price", defaultLabelStyle)
            itemAmountColTitle.setFontScale(1f)
            itemAmountColTitle.setAlignment(Align.center)

            val itemStockColTitle = Label("Stock", defaultLabelStyle)
            itemStockColTitle.setFontScale(1f)
            itemStockColTitle.setAlignment(Align.center)

            //Add the three titles
            sellItemsListTable.add(itemNameColTitle).width(100f).uniformX()
            sellItemsListTable.add(itemAmountColTitle).width(100f).uniformX()
            sellItemsListTable.add(itemStockColTitle).width(100f).uniformX()
            sellItemsListTable.add() //Empty spot for the X button
            sellItemsListTable.row()

            comp.currSellingItems.forEach { sellItemData ->
                //The item name
                val itemNameLabel = Label(sellItemData.itemName, defaultLabelStyle)
                itemNameLabel.setFontScale(1f)
                itemNameLabel.setAlignment(Align.center)

                //The item amount
                val itemAmountLabel = Label(sellItemData.itemPrice.toString(), defaultLabelStyle)
                itemAmountLabel.setFontScale(1f)
                itemAmountLabel.setAlignment(Align.center)

                /** The item stock amount */
                val itemStockTable = Table()

                val lessStockButton = TextButton("-", defaultTextButtonStyle)
                lessStockButton.label.setFontScale(1f)

                val moreStockButton = TextButton("+", defaultTextButtonStyle)
                moreStockButton.label.setFontScale(1f)

                fun getItemStockText():String =
                    if(sellItemData.itemStockAmount < 0) "max" else sellItemData.itemStockAmount.toString()

                val itemStockLabel = Label(getItemStockText(), defaultLabelStyle)
                itemStockLabel.setFontScale(1f)
                itemStockLabel.setAlignment(Align.center)

                itemStockTable.add(lessStockButton).size(16f)
                itemStockTable.add(itemStockLabel).space(0f, 5f, 0f, 5f).width(25f)
                itemStockTable.add(moreStockButton).size(16f)

                lessStockButton.addListener(object:ClickListener(){
                    override fun touchUp(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int) {
                        super.touchUp(event, x, y, pointer, button)
                        sellItemData.itemStockAmount--
                        if(sellItemData.itemStockAmount < 0) sellItemData.itemStockAmount = -1

                        itemStockLabel.setText(getItemStockText())
                    }
                })

                moreStockButton.addListener(object:ClickListener(){
                    override fun touchUp(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int) {
                        super.touchUp(event, x, y, pointer, button)
                        sellItemData.itemStockAmount++
                        itemStockLabel.setText(getItemStockText())
                    }
                })


                //TODO Need listeners for the more/less stock buttons and need to restrict amounts...

                //The x Label if we want to delete the link from a store that is reselling
                val xLabel = TextButton("X", defaultTextButtonStyle)
                xLabel.label.setFontScale(1f)
                xLabel.label.setAlignment(Align.center)

                sellItemsListTable.add(itemNameLabel).width(100f)
                sellItemsListTable.add(itemAmountLabel).width(100f)
                sellItemsListTable.add(itemStockTable)
                if(comp.resellingItemsList.size > 0) sellItemsListTable.add(xLabel).size(16f).spaceLeft(10f).right() //Either add the x label
                else sellItemsListTable.add() //Or add an empty column
                sellItemsListTable.row()

                xLabel.addListener(object:ClickListener(){
                    override fun touchUp(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int) {
                        super.touchUp(event, x, y, pointer, button)
                        if(comp.resellingItemsList.size <= 0)
                            return
                        Util.removeSellingItemFromReseller(comp, sellItemData.itemName, sellItemData.itemSourceType, sellItemData.itemSourceData)
                    }
                })
            }

            sellItemsMainTable.add(sellItemsListTable)
        }

        populateItemsTable()

        //--- Tax rate section ---

        val taxRateLabel = Label("${comp.taxRate}", defaultLabelStyle)
        taxRateLabel.setFontScale(1f)
        taxRateLabel.setAlignment(Align.center)

        val lessTaxButton = TextButton("-", defaultTextButtonStyle)
        lessTaxButton.label.setFontScale(1f)

        val moreTaxButton = TextButton("+", defaultTextButtonStyle)
        moreTaxButton.label.setFontScale(1f)

        taxRateTable.add(lessTaxButton).size(16f)
        taxRateTable.add(taxRateLabel).width(50f)
        taxRateTable.add(moreTaxButton).size(16f)

        //If we are also reselling, add this stuff
        if(comp.isReselling)
            setupResellingStuff(table, comp)

        //--- The titles for all the columns ---
        val title = Label("History:", defaultLabelStyle)
        title.setFontScale(1f)
        title.setAlignment(Align.center)

        val itemNameLabel = Label("Item", defaultLabelStyle)
        itemNameLabel.setFontScale(1f)
        itemNameLabel.setAlignment(Align.center)

        val itemAmountLabel = Label("Amt", defaultLabelStyle)
        itemAmountLabel.setFontScale(1f)
        itemAmountLabel.setAlignment(Align.center)

        val pricePerUnitLabel = Label("PPU", defaultLabelStyle)
        pricePerUnitLabel.setFontScale(1f)
        pricePerUnitLabel.setAlignment(Align.center)

        val timeStampLabel = Label("Time", defaultLabelStyle)
        timeStampLabel.setFontScale(1f)
        timeStampLabel.setAlignment(Align.center)

        val buyerNameLabel = Label("Buyer", defaultLabelStyle)
        buyerNameLabel.setFontScale(1f)
        buyerNameLabel.setAlignment(Align.center)

        //The history table function. This will populate the table
        val historyTableFunc = {
            sellHistoryTable.clear()

            //Add all the titles
            sellHistoryTable.add(title).colspan(5).fillX().expandX()
            sellHistoryTable.row()
            sellHistoryTable.add(itemNameLabel).fillX().expandX()
            sellHistoryTable.add(itemAmountLabel).fillX().expandX()
            sellHistoryTable.add(pricePerUnitLabel).fillX().expandX()
            sellHistoryTable.add(timeStampLabel).fillX().expandX()
            sellHistoryTable.add(buyerNameLabel).fillX().expandX()
            sellHistoryTable.row()

            //TODO Don't use magic number to limit this size?
            //The limit of the history
            val limit = Math.max(0, comp.sellHistory.queue.size - 5)

            //For each history, set up the labels
            for (i in (comp.sellHistory.queue.size-1).downTo(limit)){
                val sell = comp.sellHistory.queue[i]

                val _item = Label(sell.itemName, defaultLabelStyle)
                _item.setFontScale(1f)
                _item.setAlignment(Align.center)

                val _amount = Label(sell.itemAmount.toString(), defaultLabelStyle)
                _amount.setFontScale(1f)
                _amount.setAlignment(Align.center)

                val _ppu = Label(sell.pricePerItem.toString(), defaultLabelStyle)
                _ppu.setFontScale(1f)
                _ppu.setAlignment(Align.center)

                val _time = Label(sell.timeStamp.toString(), defaultLabelStyle)
                _time.setFontScale(1f)
                _time.setAlignment(Align.center)

                val _buyer = Label(sell.buyerName, defaultLabelStyle)
                _buyer.setFontScale(1f)
                _buyer.setAlignment(Align.center)

                //Add them all to the table and add a new row
                sellHistoryTable.add(_item).fillX().expandX()
                sellHistoryTable.add(_amount).fillX().expandX()
                sellHistoryTable.add(_ppu).fillX().expandX()
                sellHistoryTable.add(_time).fillX().expandX()
                sellHistoryTable.add(_buyer).fillX().expandX()
                sellHistoryTable.row()
            }
        }

        lessTaxButton.addListener(object:ChangeListener(){
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                //The 0.5 amd 4.5 are because of rounding for bad floats
                var adj = 0.5
                if(Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT))
                    adj = 4.5

                var rate = (comp.taxRate*100 - adj).toInt()
                if(rate < 0)
                    rate = 0

                comp.taxRate = rate/100f

                taxRateLabel.setText("${comp.taxRate}")
            }
        })

        moreTaxButton.addListener(object:ChangeListener(){
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                //The 1.5 amd 5.5 are because of rounding for bad floats
                var adj = 1.5
                if(Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT))
                    adj = 5.5

                var rate = (comp.taxRate*100 + adj).toInt()
                if(rate > 100)
                    rate = 100

                comp.taxRate = rate/100f

                taxRateLabel.setText("${comp.taxRate}")
            }
        })

        //Call the history table function to populate the history
        historyTableFunc()

        table.top()

        //Add all the stuff to the table
        table.add(taxRateTable).expandX().fillX().padTop(20f) //The taxCollected rate
        table.row().expandX().fillX().spaceTop(20f)
        table.add(sellItemsMainTable).expandX().fillX() //What we are selling
        table.row().expandX().fillX().spaceTop(20f)
        table.add(sellHistoryTable).expandX().fillX() //The history table
        table.row().expand().fill().spaceTop(20f) //Push everything up!

        //Put the history function into our update map
        updateMap.put("sellHistory", historyTableFunc)

        updateFuncsList.add { populateItemsTable() }

        //Put in the event system
        val entID = Mappers.identity[currentlySelectedEntity].uniqueID
        val entityEvent = GameEventSystem.subscribe<ItemSoldEvent>({historyTableFunc()}, entID) //Subscribe to the entity selling an item

        changedTabsFunc = {
            GameEventSystem.unsubscribe(entityEvent) //Unsubscribe when we leave this tab
        }

//        goldHistoryGraph.debug = true
    }

    private fun setupEconomyTable(table: Table, comp: SellingItemsComponent){
        val graphStyle = Graph.GraphStyle(TextureRegionDrawable(TextureRegion(Util.createPixel(Color.BLACK))), Color.BLACK, MyGame.defaultFont14)
        graphStyle.background = TextureRegionDrawable(TextureRegion(Util.createPixel(Color.GRAY)))
        graphStyle.graphBackground = TextureRegionDrawable(TextureRegion(Util.createPixel(Color.ORANGE)))

        val darkLabelStyle = Label.LabelStyle(MyGame.defaultFont14, Color.WHITE)
        darkLabelStyle.background = TextureRegionDrawable(TextureRegion(Util.createPixel(Color.DARK_GRAY)))

        val incomeDailyTitleLabel = Label("Daily Income", defaultLabelStyle)
        incomeDailyTitleLabel.setFontScale(1f)

        val incomeDailyLabel = Label("${comp.incomeDaily}", darkLabelStyle)
        incomeDailyLabel.setFontScale(1f)
        incomeDailyLabel.setAlignment(Align.center)

        val taxDailyTileLabel = Label("Daily Tax", defaultLabelStyle)
        taxDailyTileLabel.setFontScale(1f)

        val taxDailyLabel = Label("${comp.taxCollectedDaily}", darkLabelStyle)
        taxDailyLabel.setFontScale(1f)
        taxDailyLabel.setAlignment(Align.center)

        val goldHistoryGraph = Graph(mutableListOf(), 50, graphStyle)

        val dailyTable = Table()

        dailyTable.add(incomeDailyTitleLabel).spaceRight(20f).width(75f).uniformX().fillX()
        dailyTable.add(incomeDailyLabel).width(50f)
        dailyTable.add().growX()
        dailyTable.row()
        dailyTable.add(taxDailyTileLabel).spaceRight(20f).uniformX().fillX()
        dailyTable.add(taxDailyLabel).width(50f)
        dailyTable.add().growX()

        table.add(dailyTable).left().expandX().fillX()
        table.row()
        table.add(goldHistoryGraph).size(350f, 250f).padTop(20f)

//        table.debugAll()
        table.top()

        updateFuncsList.add {
            incomeDailyLabel.setText("${comp.incomeDaily}")
            taxDailyLabel.setText("${comp.taxCollectedDaily}")
            goldHistoryGraph.points = comp.goldHistory.queue.toList()
        }

    }

    private fun setupResellingStuff(table: Table, comp: SellingItemsComponent){
        //The link button for linking together this shop and a store
        val linkButton = TextButton("Link", defaultTextButtonStyle)
        val importButton = TextButton("Import", defaultTextButtonStyle)

        val buttonTable = Table()
        buttonTable.add(linkButton).spaceRight(50f) //The link button
        buttonTable.add(importButton) //The import button

        table.add(buttonTable).padTop(20f)
        table.row()

        //The listener for the link button
        linkButton.addListener(object: ChangeListener(){
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                val selling = Mappers.selling[entity]

                //TODO Probably want to clean this up
                guiManager.gameScreen.inputHandler.linkingAnotherEntity = true
                guiManager.gameScreen.inputHandler.linkingEntityCallback = {ent ->
                    if(ent != currentlySelectedEntity){
                        val otherBuilding = Mappers.building[ent]
                        val otherSelling = Mappers.selling[ent]

                        //If we aren't linking to a building, then don't do this...
                        if(otherBuilding != null) {
                            //TODO this needs to be more sophisticated, maybe remove the selling potential of the workshop?
                            if (otherBuilding.buildingType == BuildingComponent.BuildingType.Workshop) {
                                //Make sure we actually have stuff to add
                                if (otherSelling.currSellingItems.size > 0) {

                                    otherSelling.currSellingItems.forEach { (itemName, itemPrice) ->
                                        val sellingItemData = SellingItemData(itemName, (itemPrice * 1.5).toInt(), -1, SellingItemData.ItemSource.Workshop, ent)
                                        comp.resellingItemsList.add(sellingItemData)
                                        if(!selling.currSellingItems.any{it.itemName == itemName})
                                            selling.currSellingItems.add(sellingItemData)
                                    }

                                    otherSelling.currSellingItems.clear()
                                }
                            }
                        }
                    }
                }
            }
        })

        importButton.addChangeListener { _, _ ->
            println("Something")
            guiManager.openImportWindow(this.entity)
        }
    }
}