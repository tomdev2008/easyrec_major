package org.easyrec.store.dao.core;

import junitx.framework.Assert;
import org.easyrec.model.core.web.Item;
import org.easyrec.model.core.transfer.TimeConstraintVO;
import org.easyrec.model.core.web.RemoteTenant;
import org.easyrec.model.core.web.statistic.ItemDetails;
import org.easyrec.store.dao.core.ItemDAO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.unitils.UnitilsJUnit4TestClassRunner;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringApplicationContext;
import org.unitils.spring.annotation.SpringBeanByName;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Tests for the ItemDAO class
 */
@RunWith(UnitilsJUnit4TestClassRunner.class)
@SpringApplicationContext({
        "spring/core/dao/itemDAO.xml",
        "spring/easyrecDataSource.xml",
        "spring/core/common/rankingsCache.xml",
        "spring/utils/aop/Cache.xml"
})
public class ItemDAOTest {


    public static final int TENANT_ID = 1;
    public static final int TENANT_ID_ALTERNATIVE = 2;
    public static final String TENANT_URL = "http://www.easyrec.org/";
    public static final String ITEM_TYPE_ALTERNATIVE = "ACTION";
    public static final String[] ITEM_TYPE = {"ITEM", "ITEM"};
    public static final String[] ITEM_DESCRIPTION = {"ITEM ONE", "ITEM TWO"};
    public static final String[] ITEM_URL = {"http://www.easyrec.org/item/1", "http://www.easyrec.org/item/2"};
    public static final String[] ITEM_IMAGE_URL = {"http://www.easyrec.org/image/item1.png", "http://www.easyrec.org/image/item2.png"};

    // itemId should be different in each test case to avoid caching troubles.
    public static Integer currentItemId = 0;


    @SpringBeanByName
    protected ItemDAO itemDAO;

    @Test
    @DataSet("/dbunit/core/dao/itemDaoTest.xml")
    public void testAdd() {
        Item item = addDummyItem(0);
        Item dummyItem = createDummyItem(0);
        checkItemEquality(item, dummyItem);
    }

    @Test
    @DataSet("/dbunit/core/dao/itemDaoTest.xml")
    public void testInsertOrUpdate() {
        increaseItemId();
        // test update part of the method
        Item insertedItem = addDummyItem(0);
        Item getItem = itemDAO.get(createRemoteTenant(TENANT_ID), currentItemId.toString(), ITEM_TYPE[0]);
        Assert.assertEquals(insertedItem, getItem);
        itemDAO.insertOrUpdate(insertedItem.getTenantId(), insertedItem.getItemId(), insertedItem.getItemType(),
                ITEM_DESCRIPTION[1], ITEM_URL[1], ITEM_IMAGE_URL[1]);
        insertedItem.setDescription(ITEM_DESCRIPTION[1]);
        insertedItem.setUrl(ITEM_URL[1]);
        insertedItem.setImageUrl(ITEM_IMAGE_URL[1]);
        getItem = itemDAO.get(createRemoteTenant(TENANT_ID), currentItemId.toString(), ITEM_TYPE[0]);
        Assert.assertEquals(insertedItem, getItem);



        // test the insert part of the method
        increaseItemId();
        Item newItem = createDummyItem(1);
        itemDAO.insertOrUpdate(newItem.getTenantId(), newItem.getItemId(), newItem.getItemType(),
                newItem.getDescription(), newItem.getUrl(), newItem.getImageUrl());
        getItem = itemDAO.get(createRemoteTenant(TENANT_ID), currentItemId.toString(), ITEM_TYPE[1]);
        checkItemEquality(newItem, getItem);
    }


    @Test
    @DataSet("/dbunit/core/dao/itemDaoTest.xml")
    public void testGet() {

        // get an existing item
        increaseItemId();
        Item item = addDummyItem(0);
        Item itemLoaded = itemDAO.get(createRemoteTenant(TENANT_ID), currentItemId.toString(), ITEM_TYPE[0]);
        checkItemEquality(item, itemLoaded);

        // get a not existing item
        increaseItemId();
        itemLoaded = itemDAO.get(createRemoteTenant(TENANT_ID), currentItemId.toString(), ITEM_TYPE[0]);
        Assert.assertNull(itemLoaded);
    }

    @Test
    @DataSet("/dbunit/core/dao/itemDaoTest.xml")
    public void testExists() {
        // check existing item
        increaseItemId();
        Item item = addDummyItem(0);
        Boolean exists = itemDAO.exists(createRemoteTenant(TENANT_ID), currentItemId.toString(), item.getItemType());
        Assert.assertTrue(exists);

        //check not existing item
        increaseItemId();
        exists = itemDAO.exists(createRemoteTenant(TENANT_ID), currentItemId.toString(), item.getItemType());
        Assert.assertFalse(exists);
    }

    @Test
    @DataSet("/dbunit/core/dao/itemDaoTest.xml")
    public void testActivateAndDeactivate() {

        increaseItemId();
        Item item;
        Item initItem = addDummyItem(0);
        itemDAO.activate(TENANT_ID, currentItemId.toString(), initItem.getItemType());
        item = itemDAO.get(createRemoteTenant(TENANT_ID), currentItemId.toString(), initItem.getItemType());
        Assert.assertTrue(item.isActive());

        itemDAO.deactivate(TENANT_ID, currentItemId.toString(), initItem.getItemType());
        item = itemDAO.get(createRemoteTenant(TENANT_ID), currentItemId.toString(), initItem.getItemType());
        Assert.assertFalse(item.isActive());

        itemDAO.activate(TENANT_ID, currentItemId.toString(), initItem.getItemType());
        item = itemDAO.get(createRemoteTenant(TENANT_ID), currentItemId.toString(), initItem.getItemType());
        Assert.assertTrue(item.isActive());
    }

    @Test
    @DataSet("/dbunit/core/dao/itemDaoTest.xml")
    public void testRemove() {
        increaseItemId();
        Item initItem = addDummyItem(0);
        Item item = itemDAO.get(createRemoteTenant(TENANT_ID), currentItemId.toString(), initItem.getItemType());
        checkItemEquality(initItem, item);
        itemDAO.remove(TENANT_ID, currentItemId.toString(), initItem.getItemType());
        item = itemDAO.get(createRemoteTenant(TENANT_ID), currentItemId.toString(), initItem.getItemType());
        Assert.assertNull(item);
    }

    @Test
    @DataSet("/dbunit/core/dao/itemDaoTest.xml")
    public void testGetItems() {
        int startId =  increaseItemId();
        int endId = startId + 10;
        Item item;
        for (int itemId = startId; itemId <= endId; itemId++) {
            currentItemId = itemId;
            item = createDummyItem(0);
            addItem(item);
            item.setTenantId(TENANT_ID_ALTERNATIVE);
            addItem(item);
            item = createDummyItem(1);
            item.setItemType(ITEM_TYPE_ALTERNATIVE);
            addItem(item);
            item.setTenantId(TENANT_ID_ALTERNATIVE);
            item.setItemType(ITEM_TYPE_ALTERNATIVE);
            addItem(item);
        }

        List<Item> items = itemDAO.getItems(createRemoteTenant(TENANT_ID), ITEM_DESCRIPTION[0], 5, 1);
        Assert.assertEquals(1, items.size());
        items = itemDAO.getItems(createRemoteTenant(TENANT_ID), ITEM_DESCRIPTION[0], 5, 5);
        Assert.assertEquals(5, items.size());
        items = itemDAO.getItems(createRemoteTenant(TENANT_ID), ITEM_DESCRIPTION[0], 5, 100);
        Assert.assertEquals(6, items.size());
        items = itemDAO.getItems(createRemoteTenant(TENANT_ID), ITEM_DESCRIPTION[0], 11, 100);
        Assert.assertEquals(0, items.size());
        items = itemDAO.getItems(createRemoteTenant(TENANT_ID), ITEM_DESCRIPTION[0], 1, 10);
        Assert.assertEquals(10, items.size());
        boolean foundAll = true;
        for (Item itemIterator: items) {
            boolean foundItem = false;
            for (int itemId = startId; itemId <= endId; itemId++) {
                currentItemId = itemId;
                foundItem |= checkItemEqualityWithoutAssertions(createDummyItem(0), itemIterator);
            }
            foundAll &= foundItem;
        }
        Assert.assertTrue(foundAll);
    }

    @Test
    @DataSet("/dbunit/core/dao/itemDaoTest.xml")
    public void testRemoveItems() {
        int startId =  increaseItemId();
        int endId = startId + 10;
        Item item;
        for (int itemId = startId; itemId <= endId; itemId++) {
            currentItemId = itemId;
            item = createDummyItem(0);
            addItem(item);
            item.setTenantId(TENANT_ID_ALTERNATIVE);
            addItem(item);
        }

        itemDAO.removeItems(TENANT_ID_ALTERNATIVE);

        Item getItem;
        for (int itemId = startId; itemId <= endId; itemId++) {
            currentItemId = itemId;
            item = createDummyItem(0);
            getItem = itemDAO.get(createRemoteTenant(TENANT_ID),currentItemId.toString(), item.getItemType());
            checkItemEquality(item, getItem);
            item.setTenantId(TENANT_ID_ALTERNATIVE);
            getItem = itemDAO.get(createRemoteTenant(TENANT_ID_ALTERNATIVE),currentItemId.toString(), item.getItemType());
            Assert.assertNull(getItem);
        }
    }

    @Test
    @DataSet("/dbunit/core/dao/itemDaoTest.xml")
    public void testGetItemDetails() {
        itemDAO.emptyCache();
        currentItemId = 1;
        addDummyItem(0);
        ItemDetails itemDetails = itemDAO.getItemDetails(TENANT_ID, currentItemId.toString(), ITEM_TYPE[0]);
        Assert.assertEquals((Integer)4, itemDetails.getActions());
        Assert.assertEquals((Integer)2, itemDetails.getUsers());
        Assert.assertEquals("2011-11-11 11:11:11.0", itemDetails.getMinActionTime());
        Assert.assertEquals("2011-11-11 11:11:14.0", itemDetails.getMaxActionTime());
    }

    @Test
    @DataSet("/dbunit/core/dao/itemDaoTest.xml")
    public void testGetItemsWithRules() {
        itemDAO.emptyCache();
        currentItemId = 1;
        Item item = addDummyItem(0);
        List<Item> itemList = itemDAO.getItemsWithRules(item.getTenantId(), "", 0, 100);
        Assert.assertTrue(isItemInList(item, itemList));
    }


    @Test
    @DataSet("/dbunit/core/dao/itemDaoTest.xml")
    public void testGetNumberOfItemsWithRules() {
        itemDAO.emptyCache();
        currentItemId = 1;
        Item item = addDummyItem(0);
        Assert.assertEquals(2, itemDAO.getNumberOfItemsWithRules(item.getTenantId(), ""));
    }

    @Test
    @DataSet("/dbunit/core/dao/itemDaoTest.xml")
    public void testCount() {
        itemDAO.emptyCache();
        currentItemId = 1;
        Assert.assertEquals((Integer)1, itemDAO.count(TENANT_ID));
        Assert.assertEquals((Integer)0, itemDAO.count(TENANT_ID_ALTERNATIVE));
        Item item = addDummyItem(0);
        item.setTenantId(TENANT_ID_ALTERNATIVE);
        addItem(item);
        increaseItemId();
        item = addDummyItem(1);
        Assert.assertEquals((Integer)3, itemDAO.count(TENANT_ID));
        Assert.assertEquals((Integer)1, itemDAO.count(TENANT_ID_ALTERNATIVE));

        Assert.assertEquals((Integer)3, itemDAO.count(TENANT_ID, ""));
        Assert.assertEquals((Integer)1, itemDAO.count(TENANT_ID_ALTERNATIVE, ""));
        Assert.assertEquals((Integer)1, itemDAO.count(TENANT_ID, "ITEM ONE"));
    }

    @Test
    @DataSet("/dbunit/core/dao/itemDaoTest.xml")
    public void testGetHotItems() {

        itemDAO.emptyCache();
        currentItemId = 2;
        Item itemToFind1 = addDummyItem(0);
        increaseItemId();
        Item itemToFind2 = addDummyItem(0);

        currentItemId = 2;
        Item itemNTF = createDummyItem(1);
        itemNTF.setTenantId(TENANT_ID_ALTERNATIVE);
        addItem(itemNTF);
        increaseItemId();
        itemNTF = createDummyItem(1);
        itemNTF.setTenantId(TENANT_ID_ALTERNATIVE);
        addItem(itemNTF);

        List<Item> itemList = itemDAO.getHotItems(createRemoteTenant(TENANT_ID), 0, 100);
        Assert.assertEquals(2, itemList.size());
        Assert.assertTrue(isItemInList(itemToFind1, itemList));
    }

    @Test
    @DataSet("/dbunit/core/dao/itemDaoTest.xml")
    public void testSearchItems() {
        itemDAO.emptyCache();
        currentItemId = 2;
        Item item1;
        Item item2;
        item1 = createDummyItem(0);
        increaseItemId();
        item2 = createDummyItem(1);
        addItem(item1);
        addItem(item2);


        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        Date startDate = calendar.getTime();
        calendar.add(Calendar.DAY_OF_MONTH, 2);
        Date endDate = calendar.getTime();

        TimeConstraintVO timeConstraint = new TimeConstraintVO(startDate, endDate);
        ArrayList<String> itemTypeList = new ArrayList<String>();
        itemTypeList.add(item1.getItemType());
        List<Item> itemList = itemDAO.searchItems(TENANT_ID, item1.getItemId(),
                itemTypeList, item1.getDescription(), item1.getUrl(), item1.getImageUrl(), true,
                timeConstraint, false, "", ItemDAO.SortColumn.NONE, false, 0, 100);

        Assert.assertEquals(1, itemList.size());
        Assert.assertTrue(isItemInList(item1, itemList));
        Assert.assertFalse(isItemInList(item2, itemList));

        int resultCount = itemDAO.searchItemsTotalCount(TENANT_ID, item1.getItemId(),
                itemTypeList, item1.getDescription(), item1.getUrl(), item1.getImageUrl(), true,
                timeConstraint, false, "");
        Assert.assertEquals(1, resultCount);

        itemList = itemDAO.searchItems(TENANT_ID, item2.getItemId(),
                itemTypeList, item2.getDescription(), item2.getUrl(), item2.getImageUrl(), true,
                timeConstraint, false, "", ItemDAO.SortColumn.NONE, false, 0, 100);

        Assert.assertEquals(1, itemList.size());
        Assert.assertTrue(isItemInList(item2, itemList));
        Assert.assertFalse(isItemInList(item1, itemList));


        resultCount = itemDAO.searchItemsTotalCount(TENANT_ID, item2.getItemId(),
                itemTypeList, item2.getDescription(), item2.getUrl(), item2.getImageUrl(), true,
                timeConstraint, false, "");
        Assert.assertEquals(1, resultCount);

        // with hasRules = true only item1 should be returned

        itemList = itemDAO.searchItems(TENANT_ID, item1.getItemId(),
                itemTypeList, item1.getDescription(), item1.getUrl(), item1.getImageUrl(), true,
                timeConstraint, true, "", ItemDAO.SortColumn.NONE, false, 0, 100);

        Assert.assertEquals(1, itemList.size());
        Assert.assertTrue(isItemInList(item1, itemList));
        Assert.assertFalse(isItemInList(item2, itemList));

        resultCount = itemDAO.searchItemsTotalCount(TENANT_ID, item1.getItemId(),
                itemTypeList, item1.getDescription(), item1.getUrl(), item1.getImageUrl(), true,
                timeConstraint, true, "");
        Assert.assertEquals(1, resultCount);

        itemList = itemDAO.searchItems(TENANT_ID, item2.getItemId(),
                itemTypeList, item2.getDescription(), item2.getUrl(), item2.getImageUrl(), true,
                timeConstraint, true, "", ItemDAO.SortColumn.NONE, false, 0, 100);
        Assert.assertEquals(0, itemList.size());
        resultCount = itemDAO.searchItemsTotalCount(TENANT_ID, item2.getItemId(),
                itemTypeList, item2.getDescription(), item2.getUrl(), item2.getImageUrl(), true,
                timeConstraint, true, "");
        Assert.assertEquals(0, resultCount);
    }

    @Test
    @DataSet("/dbunit/core/dao/itemDaoTest.xml")
    public void testGetItemTypeOfItem() {
        itemDAO.emptyCache();
        currentItemId = 2;
        Item item1;
        Item item2;
        item1 = createDummyItem(0);
        increaseItemId();
        item2 = createDummyItem(1);
        item2.setItemType("ITEM_TYPE_TEST");
        addItem(item1);
        addItem(item2);

        Assert.assertEquals(1, itemDAO.getItemTypeIdOfItem(TENANT_ID, Integer.valueOf(item1.getItemId())));
        Assert.assertEquals(2, itemDAO.getItemTypeIdOfItem(TENANT_ID, Integer.valueOf(item2.getItemId())));
    }

    private boolean isItemInList(Item item, List<Item> itemList) {
        boolean isInList = false;
        for (Item itemIterator: itemList) {
            isInList |= checkItemEqualityWithoutAssertions(item, itemIterator);
        }
        return isInList;
    }


    private void checkItemEquality(Item expected, Item actual) {
        Assert.assertEquals(expected.getItemId(), actual.getItemId());
        Assert.assertEquals(expected.getItemType(), actual.getItemType());
        Assert.assertEquals(expected.getTenantId(), actual.getTenantId());
        Assert.assertEquals(expected.getDescription(), actual.getDescription());
        Assert.assertEquals(expected.getUrl(), actual.getUrl());
        Assert.assertEquals(expected.getImageUrl(), actual.getImageUrl());
    }

    private boolean checkItemEqualityWithoutAssertions(Item expected, Item actual) {
        return expected.getItemId().equals(actual.getItemId()) &&
            expected.getItemType().equals(actual.getItemType()) &&
            expected.getTenantId().equals(actual.getTenantId()) &&
            expected.getDescription().equals(actual.getDescription()) &&
            expected.getUrl().equals(actual.getUrl()) &&
            expected.getImageUrl().equals(actual.getImageUrl());
    }

    private RemoteTenant createRemoteTenant(int itemTenantId) {
        RemoteTenant remoteTenant = new RemoteTenant();
        remoteTenant.setId(itemTenantId);
        remoteTenant.setUrl(TENANT_URL);
        return remoteTenant;
    }

    private Item addDummyItem(int id) {
        return itemDAO.add(TENANT_ID, currentItemId.toString(), ITEM_TYPE[id], ITEM_DESCRIPTION[id], ITEM_URL[id], ITEM_IMAGE_URL[id]);
    }

    private Item addItem(Item item) {
        return itemDAO.add(item.getTenantId(), item.getItemId(), item.getItemType(), item.getDescription(), item.getUrl(), item.getImageUrl());
    }

    private int increaseItemId(){
       return ++currentItemId;
    }

    private Item createDummyItem(int id) {
        Item item = new Item();
        item.setItemId(currentItemId.toString());
        item.setTenantId(TENANT_ID);
        item.setItemType(ITEM_TYPE[id]);
        item.setDescription(ITEM_DESCRIPTION[id]);
        item.setUrl(ITEM_URL[id]);
        item.setImageUrl(ITEM_IMAGE_URL[id]);
        return item;
    }


}
