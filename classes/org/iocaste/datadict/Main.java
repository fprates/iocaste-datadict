package org.iocaste.datadict;

import java.util.HashMap;
import java.util.Map;

import org.iocaste.documents.common.DataElement;
import org.iocaste.documents.common.DataType;
import org.iocaste.documents.common.DocumentModel;
import org.iocaste.documents.common.DocumentModelItem;
import org.iocaste.documents.common.DocumentModelKey;
import org.iocaste.documents.common.Documents;
import org.iocaste.shell.common.AbstractPage;
import org.iocaste.shell.common.Button;
import org.iocaste.shell.common.CheckBox;
import org.iocaste.shell.common.Const;
import org.iocaste.shell.common.Container;
import org.iocaste.shell.common.DataForm;
import org.iocaste.shell.common.DataItem;
import org.iocaste.shell.common.Element;
import org.iocaste.shell.common.Form;
import org.iocaste.shell.common.InputComponent;
import org.iocaste.shell.common.ListBox;
import org.iocaste.shell.common.SearchHelp;
import org.iocaste.shell.common.Shell;
import org.iocaste.shell.common.Table;
import org.iocaste.shell.common.TableColumn;
import org.iocaste.shell.common.TableItem;
import org.iocaste.shell.common.Text;
import org.iocaste.shell.common.ViewData;
import org.iocaste.transport.common.Order;
import org.iocaste.transport.common.Transport;

public class Main extends AbstractPage {
    private static final byte CREATE = 0;
    private static final byte SHOW = 1;
    private static final byte UPDATE = 2;
    
    private static final byte MODELNAME = 0;
    private static final byte MODELCLASS = 1;
    private static final byte MODELTABLE = 2;
    
    private static final boolean OBLIGATORY = true;
    private static final boolean NON_OBLIGATORY = false;
    
    private enum ItensNames {
        NAME("item.name", "DATAELEMENT.NAME"),
        TABLE_FIELD("item.tablefield", "MODELITEM.FIELDNAME"),
        CLASS_FIELD("item.classfield", "MODELITEM.ATTRIB"),
        KEY("item.key", null), 
        TYPE("item.type", null),
        LENGTH("item.length", "DATAELEMENT.LENGTH"),
        TEXT("item.text", null);
        
        private String name, de;
        
        ItensNames(String name, String de) {
            this.name = name;
            this.de = de;
        }
        
        public final String getDataElement() {
            return de;
        }
        
        public final String getName() {
            return name;
        }
    };
    
    /**
     * 
     * @param vdata
     */
    public final void add(ViewData vdata) throws Exception {
        byte mode = getMode(vdata);
        Table itens = (Table)vdata.getElement("itens");
        Map<ItensNames, DataElement> references = getFieldReferences();
        
        if (hasItemDuplicated(vdata))
            return;
        
        insertItem(itens, mode, null, references);
    }
    
    /**
     * 
     * @param vdata
     * @throws Exception
     */
    public final void create(ViewData vdata) throws Exception {
        Documents documents = new Documents(this);
        String modelname = ((DataItem)vdata.getElement("modelname")).getValue();
        
        if (documents.hasModel(modelname)) {
            vdata.message(Const.ERROR, "model.already.exist");
            return;
        }
        
        vdata.setReloadableView(true);
        vdata.export("mode", CREATE);
        vdata.export("modelname", modelname);
        vdata.export("model", null);
        vdata.redirect(null, "structure");
    }
    
    /**
     * 
     * @param vdata
     * @throws Exception
     */
    public final void delete(ViewData vdata) throws Exception {
        Documents documents = new Documents(this);
        String modelname = ((DataItem)vdata.getElement("modelname")).getValue();
        
        if (!documents.hasModel(modelname)) {
            vdata.message(Const.ERROR, "model.not.found");
            return;
        }
        
        documents.removeModel(modelname);
        documents.commit();
        
        vdata.message(Const.STATUS, "model.removed.sucessfully");
    }
    
    /**
     * 
     * @param vdata
     */
    public final void deleteitem(ViewData vdata) {
        Table itens = (Table)vdata.getElement("itens");
        
        for (TableItem item : itens.getItens())
            if (item.isSelected())
                itens.remove(item);
    }
    
    /**
     * 
     * @return
     */
    private final Map<ItensNames, DataElement> getFieldReferences()
            throws Exception {
        Map<ItensNames, DataElement> references =
                new HashMap<ItensNames, DataElement>();
        Documents docs = new Documents(this);
        
        for (ItensNames itemname : ItensNames.values())
            references.put(itemname,
                    docs.getDataElement(itemname.getDataElement()));
        
        return references;
    }
    
    /**
     * 
     * @param vdata
     * @return
     */
    private final byte getMode(ViewData vdata) {
        return (Byte)vdata.getParameter("mode");
    }
    
    /**
     * 
     * @param vdata
     * @return
     */
    private final String getModelName(ViewData vdata) {
        byte mode = getMode(vdata);
        DocumentModel model = (DocumentModel)vdata.getParameter("model");
        
        if (mode == CREATE)
            return ((String)vdata.getParameter("modelname"));
        else
            return model.getName();
    }
    
    /**
     * 
     * @param item
     * @param name
     * @return
     */
    private final String getTableValue(TableItem item, String name) {
        InputComponent input = (InputComponent)item.get(name);
        
        return input.getValue();
    }
    
    /**
     * 
     * @param vdata
     * @return
     */
    private final boolean hasItemDuplicated(ViewData vdata) {
        String name, classfield, tablefield;
        String testname, testclassfield, testtablefield;
        Table itens = (Table)vdata.getElement("itens");
        
        for (TableItem item : itens.getItens()) {
            name = getTableValue(item, "item.name");
            classfield = getTableValue(item, "item.classfield");
            tablefield = getTableValue(item, "item.tablefield");
            
            for (TableItem test : itens.getItens()) {
                if (item == test)
                    continue;
                
                testname = getTableValue(test, "item.name");
                testclassfield = getTableValue(test, "item.classfield");
                testtablefield = getTableValue(test, "item.tablefield");
                
                if (name.equals(testname)) {
                    vdata.message(Const.ERROR, "item.name.duplicated");
                    return true;
                }

                if (classfield.equals(testclassfield)) {
                    vdata.message(Const.ERROR, "item.classfield.duplicated");
                    return true;
                }
                
                if (tablefield.equals(testtablefield)) {
                    vdata.message(Const.ERROR, "item.tablefield.duplicated");
                    return true;
                }
                    
            }
        }
        
        return false;
    }
    
    /**
     * 
     * @param itens
     * @param mode
     * @param modelitem
     */
    private final void insertItem(Table itens, byte mode,
            DocumentModelItem modelitem,
            Map<ItensNames, DataElement> references) {
        ListBox list;
        DocumentModel model;
        DataElement dataelement = (modelitem == null)?
                null : modelitem.getDataElement();
        FieldHelper helper = new FieldHelper();
        
        helper.item = new TableItem(itens);
        helper.mode = mode;
        
        for (ItensNames itemname : ItensNames.values()) {
            helper.name = itemname.getName();
            helper.reference = references.get(itemname);
            
            if (helper.name.equals("item.tablefield")) {
                helper.type = Const.TEXT_FIELD;
                helper.value = (modelitem == null)?
                        null:modelitem.getTableFieldName();
                helper.obligatory = OBLIGATORY;
                
                newField(helper);
                
                continue;
            }
            
            if (helper.name.equals("item.classfield")) {
                helper.type = Const.TEXT_FIELD;
                helper.value = (modelitem == null)?
                        null:modelitem.getAttributeName();
                helper.obligatory = OBLIGATORY;

                newField(helper);
                
                continue;
            }
            
            if (helper.name.equals("item.name")) {
                helper.type = Const.TEXT_FIELD;
                helper.value = (modelitem == null)?null:modelitem.getName();
                helper.obligatory = OBLIGATORY;

                newField(helper);
                
                continue;
            }
            
            if (helper.name.equals("item.length")) {
                helper.type = Const.TEXT_FIELD;
                helper.value = (modelitem == null)?null:Integer.toString(
                        dataelement.getLength());
                helper.obligatory = OBLIGATORY;

                newField(helper);
                
                continue;
            }
            
            if (helper.name.equals("item.key")) {
                helper.type = Const.CHECKBOX;
                helper.obligatory = NON_OBLIGATORY;
                
                if (modelitem != null) {
                    model = modelitem.getDocumentModel();
                    if (mode == SHOW)
                        helper.value = model.isKey(modelitem)? "yes" : "no";
                    else
                        helper.value =  model.isKey(modelitem)? "no" : "off";
                } else {
                    helper.value = (mode == SHOW)?"no" : "off";
                }
                
                newField(helper);
                
                continue;
            }
        
            if (helper.name.equals("item.type")) {
                helper.obligatory = NON_OBLIGATORY;
                
                if (mode == SHOW) {
                    helper.type = Const.TEXT;
                    
                    switch (dataelement.getType()) {
                    case 0:
                        helper.value = "char";
                        break;
                    case 3:
                        helper.value = "numc";
                        break;
                    default:
                        helper.value = "?";
                        break;
                    }
                    
                    newField(helper);
                    
                } else {
                    helper.type = Const.LIST_BOX;
                    helper.value = (modelitem == null)?null:Integer.toString(
                            dataelement.getType());
                    
                    list = (ListBox)newField(helper);
                    list.add("char", Integer.toString(DataType.CHAR));
                    list.add("numc", Integer.toString(DataType.NUMC));
                }
                
                continue;
            }
        }
    }
    
    /**
     * 
     * @param view
     */
    public final void main(ViewData view) {
        Container main = new Form(null, "datadict.main");
        DataForm modelform = new DataForm(main, "modelform");
        DataItem modelname = new DataItem(modelform, Const.TEXT_FIELD,
                "modelname");
        DataElement dataelement = new DataElement();
        SearchHelp search = new SearchHelp(main, "tablename");
        
        dataelement.setUpcase(true);
        dataelement.setLength(20);
        dataelement.setType(DataType.CHAR);
        
        search.setText("table.name.search");
        search.setModelName("MODEL");
        search.addModelItemName("NAME");
        search.setExport("NAME");
        
        modelname.setSearchHelp(search);
        modelname.setDataElement(dataelement);
        modelname.setObligatory(true);
        
        modelform.addAction("create");
        modelform.addAction("show");
        modelform.addAction("update");
        modelform.addAction("delete");
        
        view.setFocus("modelname");
        view.setNavbarActionEnabled("back", true);
        view.setTitle("datadict-selection");
        view.addContainer(main);
    }
    
    /**
     * 
     * @param helper
     * @return
     */
    private final Element newField(FieldHelper helper) {
        Element element;
        InputComponent input;
        Table table = helper.item.getTable();
        
        if (helper.mode == SHOW) {
            element = Shell.factory(table, Const.TEXT, helper.name, null);
            ((Text)element).setText(helper.value);
        } else {
            element = Shell.factory(table, helper.type, helper.name, null);
            
            input = (InputComponent)element;
            input.setValue(helper.value);
            input.setDataElement(helper.reference);
            input.setObligatory(helper.obligatory);
        }
        
        helper.item.add(element);
        
        return element;
    }
    /**
     * 
     * @param form
     * @param modelname
     * @param model
     * @param mode
     */
    private final void prepareHeader(DataForm form, String modelname,
            DocumentModel model, byte mode) throws Exception {
        String name;
        DataItem dataitem;
        DataElement[] references = new DataElement[3];
        Documents docs = new Documents(this);
        
        references[MODELNAME] = docs.getDataElement("MODEL.NAME");
        references[MODELTABLE] = docs.getDataElement("MODEL.TABLE");
        references[MODELCLASS] = docs.getDataElement("MODEL.CLASS");
        
        for (Element element : form.getElements()) {
            if (!element.isDataStorable())
                continue;
            
            dataitem = (DataItem)element;
            name = dataitem.getName();
            
            if (name.equals("modelname")) {
                dataitem.setObligatory(false);
                dataitem.setValue(modelname);
                dataitem.setEnabled(false);
                dataitem.setDataElement(references[MODELNAME]);
                
                continue;
            }
            
            if (name.equals("modeltable")) {
                dataitem.setEnabled((mode == SHOW)?false:true);
                dataitem.setObligatory((mode == SHOW)?false:true);
                dataitem.setDataElement(references[MODELTABLE]);
                
                if (model == null)
                    continue;
                
                dataitem.setValue(model.getTableName());
                continue;
            }
            
            if (name.equals("modelclass")) {
                dataitem.setEnabled((mode == SHOW)?false:true);
                dataitem.setObligatory((mode == SHOW)?false:true);
                dataitem.setDataElement(references[MODELCLASS]);
                
                if (model == null)
                    continue;
                
                dataitem.setValue(model.getClassName());
                continue;
            }
        }
    }
    
    /**
     * 
     * @param itens
     */
    private final void prepareItens(Table itens, byte mode,
            DocumentModel model) throws Exception {
        Map<ItensNames, DataElement> references = getFieldReferences();
        
        if (model == null)
            insertItem(itens, mode, null, references);
        else
            for (DocumentModelItem modelitem : model.getItens())
                insertItem(itens, mode, modelitem, references);
    }
    
    /**
     * 
     * @param vdata
     * @throws Exception
     */
    public final void save(ViewData vdata) throws Exception {
        DocumentModelItem modelitem;
        DocumentModelKey modelkey;
        DataElement dataelement;
        String itemname;
        CheckBox key;
        Documents documents = new Documents(this);
        DataForm structure = (DataForm)vdata.getElement("structure.form");
        Table itens = (Table)vdata.getElement("itens");
        DocumentModel model = new DocumentModel();
        byte mode = getMode(vdata);
        int i = 0;
        
        if (hasItemDuplicated(vdata))
            return;
        
        model.setName(structure.getValue("modelname"));
        model.setClassName(structure.getValue("modelclass"));
        model.setTableName(structure.getValue("modeltable"));
        
        for (TableItem item : itens.getItens()) {
            itemname = getTableValue(item, "item.name");
            
            dataelement = new DataElement();
            dataelement.setName(new StringBuilder(model.getName()).append(".").
                    append(itemname).toString());
            dataelement.setLength(Integer.parseInt(getTableValue(
                    item, "item.length")));
            dataelement.setType(Integer.parseInt(getTableValue(
                    item, "item.type")));
             
            modelitem = new DocumentModelItem();
            modelitem.setIndex(i++);
            modelitem.setName(itemname);
            modelitem.setTableFieldName(getTableValue(item, "item.tablefield"));
            modelitem.setAttributeName(getTableValue(item, "item.classfield"));
            modelitem.setDataElement(dataelement);
            modelitem.setDocumentModel(model);
            
            model.add(modelitem);
            
            key = (CheckBox)item.get("item.key");
            if (!key.isSelected())
                continue;
        
            modelkey = new DocumentModelKey();
            modelkey.setModel(model);
            modelkey.setModelItem(itemname);
            
            model.addKey(modelkey);
        }
        
        switch (mode) {
        case UPDATE:
            documents.updateModel(model);
            
            break;
        case CREATE:
            documents.createModel(model);
            vdata.export("model", model);
            vdata.export("mode", UPDATE);
            vdata.setReloadableView(true);
            
            break;
        }
        
        documents.commit();
        
        vdata.message(Const.STATUS, "table.saved.successfully");
    }
    
    /**
     * 
     * @param vdata
     * @throws Exception 
     */
    public final void show(ViewData vdata) throws Exception {
        DocumentModel model;
        String modelname = ((DataItem)vdata.getElement("modelname")).getValue();
        Documents documents = new Documents(this);
        
        if (!documents.hasModel(modelname)) {
            vdata.message(Const.ERROR, "model.not.found");
            return;
        }
        
        model = documents.getModel(modelname);
        
        vdata.setReloadableView(true);
        vdata.export("mode", SHOW);
        vdata.export("model", model);
        vdata.redirect(null, "structure");
    }
    
    /**
     * 
     * @param vdata
     */
    public final void structure(ViewData vdata) throws Exception {
        String title, modelname;
        TableColumn column;
        DocumentModel usermodel = (DocumentModel)vdata.getParameter("model");
        byte mode = getMode(vdata);
        Container main = new Form(null, "datadict.structure");
        DataForm structure = new DataForm(main, "structure.form");
        Table itens = new Table(main, "itens");
        Map<ItensNames, DataElement> references = getFieldReferences();
        
        modelname = getModelName(vdata);
        new DataItem(structure, Const.TEXT_FIELD, "modelname");
        new DataItem(structure, Const.TEXT_FIELD, "modeltext");
        new DataItem(structure, Const.TEXT_FIELD, "modelclass");
        new DataItem(structure, Const.TEXT_FIELD, "modeltable");
        
        prepareHeader(structure, modelname, usermodel, mode);
        
        for (ItensNames itemname : ItensNames.values()) {
            column = new TableColumn(itens, itemname.getName());
            column.setDataElement(references.get(itemname));
        }
        
        itens.setMark(true);
        prepareItens(itens, mode, usermodel);
        
        switch (mode) {
        case UPDATE:
            itens.setMark(true);
            title = "datadict-update";
            new Button(main, "save");
            new Button(main, "add");
            new Button(main, "deleteitem");
            break;
        
        case SHOW:
            itens.setMark(false);
            title = "datadict-view";
            break;
        
        case CREATE:
            itens.setMark(true);
            title = "datadict-create";
            new Button(main, "save");
            new Button(main, "add");
            new Button(main, "deleteitem");
            break;
            
        default:
            title = null;
        }
        
        vdata.setFocus("modeltext");
        vdata.setNavbarActionEnabled("back", true);
        vdata.setTitle(title);
        vdata.addContainer(main);
    }
    
    /**
     * 
     * @param vdata
     */
    public final void transport(ViewData vdata) throws Exception {
        Transport transport = new Transport(this);
        Order order = new Order();
        
        transport.save(order);
      
        vdata.message(Const.STATUS, "object.transport.successful");
    }
    
    /**
     * 
     * @param vdata
     * @throws Exception
     */
    public final void update(ViewData vdata) throws Exception {
        DocumentModel model;
        String modelname = ((DataItem)vdata.getElement("modelname")).getValue();
        Documents documents = new Documents(this);
        
        if (!documents.hasModel(modelname)) {
            vdata.message(Const.ERROR, "model.not.found");
            return;
        }
        
        model = documents.getModel(modelname);
        
        vdata.setReloadableView(true);
        vdata.export("mode", UPDATE);
        vdata.export("model", model);
        vdata.redirect(null, "structure");
    }
}

class FieldHelper {
    public Const type;
    public int mode;
    public TableItem item;
    public String name, value;
    public DataElement reference;
    public boolean obligatory;
}