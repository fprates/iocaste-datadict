package org.iocaste.datadict;

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
import org.iocaste.shell.common.TableItem;
import org.iocaste.shell.common.ViewData;

public class Main extends AbstractPage {
    private static final byte CREATE = 0;
    private static final byte SHOW = 1;
    private static final byte UPDATE = 2;
    
    private static final String[] HEADER_NAMES = {
        "modelname",
        "modeltext",
        "modelclass",
        "modeltable"
    };
    
    private static final String[] ITEM_NAMES = {
        "item.name",
        "item.tablefield",
        "item.classfield",
        "item.key", 
        "item.type",
        "item.length",
        "item.text"
    };
    
    /**
     * 
     * @param vdata
     */
    public final void add(ViewData vdata) {
        byte mode = getMode(vdata);
        Table itens = (Table)vdata.getElement("itens");
        
        insertItem(itens, mode, null);
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
     * @param name
     * @return
     */
    private final DocumentModel getHeaderModel(String name) {
        DocumentModelItem modelitem;
        DataElement dataelement;
        DocumentModel model = new DocumentModel();
        int i = 0;
        
        for (String headername : HEADER_NAMES) {
            dataelement = new DataElement();
            dataelement.setLength(20);
            
            if (headername.equals("modelclass"))
                dataelement.setUpcase(false);
            else
                dataelement.setUpcase(true);
            
            modelitem = new DocumentModelItem();
            modelitem.setIndex(i++);
            modelitem.setName(headername);
            modelitem.setDocumentModel(model);
            modelitem.setDataElement(dataelement);
            
            model.setName(name);
            model.add(modelitem);
        }
        
        return model;
        
    }
    
    /**
     * 
     * @param modelname
     * @return
     */
    private final DocumentModel getItensModel(String modelname) {
        DataElement dataelement;
        DocumentModelItem item;
        DocumentModel model = new DocumentModel();
        int i = 0;
        
        model.setName(modelname);
        
        for (String name : ITEM_NAMES) {
            dataelement = new DataElement();
            dataelement.setUpcase(true);
            dataelement.setType(DataType.CHAR);
            dataelement.setLength(20);
            
            if (name.equals("item.length")) {
                dataelement.setType(DataType.NUMC);
                dataelement.setLength(3);
            }
            
            if (name.equals("item.classname"))
                dataelement.setUpcase(false);
            
            item = new DocumentModelItem();
            item.setName(name);
            item.setDocumentModel(model);
            item.setDataElement(dataelement);
            item.setIndex(i++);
            
            model.add(item);
        }
        
        return model;
    }
    
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
     * @param itens
     * @param mode
     * @param modelitem
     */
    private final void insertItem(Table itens, byte mode,
            DocumentModelItem modelitem) {
        ListBox list;
        DocumentModel model;
        String value;
        DataElement dataelement = (modelitem == null)?
                null : modelitem.getDataElement();
        TableItem item = new TableItem(itens);
        
        for (String name: ITEM_NAMES) {
            if (name.equals("item.tablefield")) {
                value = (modelitem == null)?null:modelitem.getTableFieldName();
                newField(Const.TEXT_FIELD, mode, item, name, value);
                
                continue;
            }
            
            if (name.equals("item.classfield")) {
                value = (modelitem == null)?null:modelitem.getAttributeName();
                newField(Const.TEXT_FIELD, mode, item, name, value);
                
                continue;
            }
            
            if (name.equals("item.name")) {
                value = (modelitem == null)?null:modelitem.getName();
                newField(Const.TEXT_FIELD, mode, item, name, value);
                
                continue;
            }
            
            if (name.equals("item.length")) {
                value = (modelitem == null)?null:Integer.toString(
                        dataelement.getLength());
                newField(Const.TEXT_FIELD, mode, item, name, value);
                
                continue;
            }
            
            if (name.equals("item.key")) {
                if (modelitem != null) {
                    model = modelitem.getDocumentModel();
                    value =  model.isKey(modelitem)? "on" : "off";
                } else {
                    value = "off";
                }
                
                newField(Const.CHECKBOX, mode, item, name, value);
                
                continue;
            }
        
            if (name.equals("item.type")) {
                list = (ListBox)newField(Const.LIST_BOX, mode, item, name,
                        null);
                
                list.add("char", Integer.toString(DataType.CHAR));
                list.add("numc", Integer.toString(DataType.NUMC));
                
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
    
    private final Element newField(Const type, int mode, TableItem item,
            String name, String value) {
        Element element = Shell.factory(item.getTable(), type, name, null);
        InputComponent input = (InputComponent)element;
        
        item.add(element);
        input.setValue(value);
        element.setEnabled((mode == SHOW)?false:true);
        
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
            DocumentModel model, byte mode) {
        String name;
        DataItem dataitem;
        
        for (Element element : form.getElements()) {
            if (!element.isDataStorable())
                continue;
            
            dataitem = (DataItem)element;
            name = dataitem.getName();
            
            if (name.equals("modelname")) {
                dataitem.setObligatory(false);
                dataitem.setValue(modelname);
                dataitem.setEnabled(false);
                continue;
            }
            
            if (name.equals("modeltable")) {
                dataitem.setEnabled((mode == SHOW)?false:true);
                dataitem.setObligatory((mode == SHOW)?false:true);
                
                if (model == null)
                    continue;
                
                dataitem.setValue(model.getTableName());
                continue;
            }
            
            if (name.equals("modelclass")) {
                dataitem.setEnabled((mode == SHOW)?false:true);
                dataitem.setObligatory((mode == SHOW)?false:true);
                
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
    private final void prepareItens(Table itens, byte mode, DocumentModel model) {
        if (model == null)
            insertItem(itens, mode, null);
        else
            for (DocumentModelItem modelitem : model.getItens())
                insertItem(itens, mode, modelitem);
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
            vdata.message(Const.ERROR, "model.doesnt.exists");
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
    public final void structure(ViewData vdata) {
        String title, modelname;
        DocumentModel model, usermodel =
                (DocumentModel)vdata.getParameter("model");
        byte mode = getMode(vdata);
        Container main = new Form(null, "datadict.structure");
        DataForm structure = new DataForm(main, "structure.form");
        Table itens = new Table(main, "itens");
        
        modelname = getModelName(vdata);
        model = getHeaderModel(modelname);
        structure.importModel(model);
        prepareHeader(structure, modelname, usermodel, mode);
        
        model = getItensModel("itens");
        itens.setMark(true);
        itens.importModel(model);
        prepareItens(itens, mode, usermodel);
        
        switch (mode) {
        case UPDATE:
            title = "datadict-update";
            new Button(main, "save");
            new Button(main, "add");
            new Button(main, "deleteitem");
            break;
        
        case SHOW:
            title = "datadict-view";
            break;
        
        case CREATE:
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
//        String transportdir = getRealPath("../../../transport");
//        File file = new File(transportdir+"/ot0001.txt");
//        FileWriter fwriter = new FileWriter(file);
//        BufferedWriter bwriter = new BufferedWriter(fwriter);
//        
//        bwriter.write("IOCST_OT");
//        bwriter.newLine();
//        bwriter.flush();
//        bwriter.close();
//        
//        vdata.message(Const.STATUS, "object.transport.successful");
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
            vdata.message(Const.ERROR, "model.doesnt.exists");
            return;
        }
        
        model = documents.getModel(modelname);
        
        vdata.setReloadableView(true);
        vdata.export("mode", UPDATE);
        vdata.export("model", model);
        vdata.redirect(null, "structure");
    }
}
