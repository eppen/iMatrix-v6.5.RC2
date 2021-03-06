package com.norteksoft.mms.form.service;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.norteksoft.acs.base.enumeration.FunctionType;
import com.norteksoft.acs.entity.authorization.BusinessSystem;
import com.norteksoft.acs.entity.authorization.Function;
import com.norteksoft.acs.service.authorization.BusinessSystemManager;
import com.norteksoft.acs.service.authorization.FunctionGroupManager;
import com.norteksoft.acs.service.authorization.FunctionManager;
import com.norteksoft.mms.base.GenerateCodeUtils;
import com.norteksoft.mms.base.autoTool.MyClassLoader;
import com.norteksoft.mms.base.autoTool.Util;
import com.norteksoft.mms.base.autoTool.dataTable.AnnotationAnalysis;
import com.norteksoft.mms.base.autoTool.dataTable.DataTableAnnotationFactory;
import com.norteksoft.mms.form.dao.DataTableDao;
import com.norteksoft.mms.form.dao.GenerateSettingDao;
import com.norteksoft.mms.form.dao.ListColumnDao;
import com.norteksoft.mms.form.dao.TableColumnDao;
import com.norteksoft.mms.form.entity.DataTable;
import com.norteksoft.mms.form.entity.FormControl;
import com.norteksoft.mms.form.entity.FormView;
import com.norteksoft.mms.form.entity.GenerateSetting;
import com.norteksoft.mms.form.entity.ListView;
import com.norteksoft.mms.form.entity.TableColumn;
import com.norteksoft.mms.form.enumeration.DataType;
import com.norteksoft.mms.form.enumeration.RelationSetting;
import com.norteksoft.mms.form.jdbc.JdbcSupport;
import com.norteksoft.mms.module.dao.MenuDao;
import com.norteksoft.mms.module.entity.Menu;
import com.norteksoft.product.api.ApiFactory;
import com.norteksoft.product.enumeration.DataState;
import com.norteksoft.product.orm.Page;
import com.norteksoft.product.util.ContextUtils;
import com.norteksoft.product.util.PropUtils;
import com.norteksoft.product.web.struts2.Struts2Utils;

@Service
public class DataTableManager {

	private DataTableDao dataTableDao;
	private TableColumnDao tableColumnDao;
	private ListColumnDao listColumnDao;
	private JdbcSupport jdbcDao;
	private FormViewManager formViewManager;
	private ListViewManager listViewManager;
	private GenerateSettingDao generateSettingDao;
	private Log log = LogFactory.getLog(DataTableManager.class);
	private FormHtmlParser formHtmlParser = new FormHtmlParser();
	
	@Autowired
	private FunctionManager functionManager;
	@Autowired
	private FunctionGroupManager functionGroupManager;
	@Autowired
	private MenuDao menuDao;
	@Autowired
	private BusinessSystemManager businessSystemManager;
	

	@Autowired
	public void setDataTableDao(DataTableDao dataTableDao) {
		this.dataTableDao = dataTableDao;
	}

	@Autowired
	public void setTableColumnDao(TableColumnDao tableColumnDao) {
		this.tableColumnDao = tableColumnDao;
	}
	
	@Autowired
	public void setFormViewManager(FormViewManager formViewManager) {
		this.formViewManager = formViewManager;
	}
	
	@Autowired
	public void setListViewManager(ListViewManager listViewManager) {
		this.listViewManager = listViewManager;
	}
	@Autowired
	public void setGenerateSettingDao(GenerateSettingDao generateSettingDao) {
		this.generateSettingDao = generateSettingDao;
	}
	@Autowired
	public void setListColumnDao(ListColumnDao listColumnDao) {
		this.listColumnDao = listColumnDao;
	}
	
	public void setJdbcDao(JdbcSupport jdbcDao) {
		this.jdbcDao = jdbcDao;
	}

	/**
	 * 查询数据表实体
	 * 
	 * @param dataTableId
	 * @return 数据表实体
	 */
	public DataTable getDataTable(Long dataTableId) {
		return dataTableDao.get(dataTableId);
	}

	/**
	 * 查询所有的数据表(以createDate排序)
	 * 
	 * @param tables
	 */
	public void getAllDataTables(Page<DataTable> tables) {
		dataTableDao.findAllDataTable(tables);
	}

	/**
	 * 查询所有启用的数据表(以createDate排序)
	 * 
	 * @param tables
	 */
	public void getAllEnableDataTables(Page<DataTable> tables) {
		dataTableDao.findAllEnabledDataTable(tables);
	}

	/**
	 * 查询启用的数据表集合(以createDate排序)
	 * 
	 * @return 返回数据表集合
	 */
	public List<DataTable> getEnabledDataTables() {
		return dataTableDao.getEnabledDataTables();
	}
	
	/**
	 * 查询启用的数据表集合(以createDate排序)
	 * 
	 * @return 返回数据表集合
	 */
	public List<DataTable> getAllEnabledDataTables() {
		return dataTableDao.getAllEnabledDataTables();
	}

	/**
	 * 通过表名查询数据表实体
	 * 
	 * @param tableName
	 * @param tableId
	 * @return 返回是否验证成功
	 */
	public boolean getDataTableByName(String tableName, Long tableId) {
		DataTable dataTable = dataTableDao.findDataTableByName(tableName);
		if (tableId != null) { // 修改
			if (dataTable != null) {
				if (dataTable.getId().longValue() == tableId.longValue()) {
					return true; // 通过
				} else {
					return false; // 不通过
				}
			} else {
				return true;
			}
		} else { // 新建
			if (dataTable != null) {
				return false;
			} else {
				return true;
			}
		}
	}

	/**
	 * 查询数据表字段
	 * 
	 * @param tableColumnId
	 * @return 返回表字段实体
	 */
	public TableColumn getTableColumn(Long tableColumnId) {
		return tableColumnDao.get(tableColumnId);
	}

	/**
	 * 删除草稿状态的数据表
	 * 
	 * @param ids
	 */
	@Transactional(readOnly = false)
	public void deleteDataTables(List<Long> ids) {
		for (Long long1 : ids) {
			dataTableDao.delete(long1);
		}
	}
	
	@Transactional(readOnly = false)
	public void deleteEnableDataTables(List<Long> ids) {
		for (Long long1 : ids) {
			boolean isCompleteDeletedForm=false;
			// 删列表、表单、页面
			List<FormView> fvs = formViewManager.getFormViewByDataTable(long1);
			for(FormView fv : fvs){
				if(!fv.isStandardForm()){//是自定义表单时才会在删除表单时同时删除数据表
					isCompleteDeletedForm=true;
				}
				formViewManager.deleteFormViewComplete(fv.getId());
			}
			List<ListView> lvs = listViewManager.getListViewByTabelId(long1);
			for(ListView lv : lvs){
				listColumnDao.deleteListColumnsByView(lv.getId());
				listViewManager.deleteEnable(lv.getId());
			}
			//删除数据表对应的字段
			tableColumnDao.deleteTableColumnsByTable(long1);
			//删除数据表
			if(!isCompleteDeletedForm)
			dataTableDao.delete(long1);
		}
	}
	/**
	 * 保存数据表信息
	 * 
	 * @param dataTable
	 */
	@Transactional(readOnly = false)
	public void saveDataTable(DataTable dataTable) {
		if (dataTable.getId() == null) {
			dataTable.setCompanyId(ContextUtils.getCompanyId());
			dataTable.setCreator(ContextUtils.getLoginName());
			dataTable.setCreatorName(ContextUtils.getUserName());
			if(dataTable.getTableState()==null||"".equals(dataTable.getTableState())){
				dataTable.setTableState(DataState.DRAFT);
			}
		}
		dataTable.setCreatedTime(new Date());
		dataTableDao.save(dataTable);
	}

	/**
	 * 获取所有未删除的表字段
	 * 
	 * @param dataTable
	 * @return 返回显示的表字段
	 */
	public List<TableColumn> getAllUnDeleteColumns(DataTable dataTable) {
		List<TableColumn> tableColumns = tableColumnDao.getTableColumnByDataTableId(dataTable.getId());
		return tableColumns;
	}

	/**
	 * 创建数据表
	 * 
	 * @param dataTable
	 */
	@Transactional(readOnly = false)
	public void createTable(FormView formView) {
		try{
			String tableName ="mms_"+formView.getCode();
			List<FormControl> controls=formHtmlParser.getControls(formView.getHtml(),false);
			log.debug("begin to create table :" + tableName);
			jdbcDao.createDefaultTable(tableName, controls);
			log.debug("create table " + tableName + " end");
//			log.debug("begin to create sequence :" + tableName + "_ID");
//			jdbcDao.createSequence(tableName + "_ID");
		}catch (BadSqlGrammarException e) {
			log.debug(e.getMessage());
		}
	}
	
	/**
	 * 根据实体名获取数据表
	 * @param entityName
	 * @return
	 */
	public DataTable getDataTableByEntity(String entityName){
		return dataTableDao.getDataTableByEntity(entityName);
	}

	/**
	 * 生成默认视图
	 * 
	 * @param dataTable
	 */
	@Transactional(readOnly = false)
	public void createDefaultView(DataTable dataTable, Long menuId) {
		String tableAlias = dataTable.getAlias();
		String name = dataTable.getName();
		String remark = dataTable.getRemark();
		FormView formView = new FormView();
		formView.setDataTable(dataTable);
		formView.setCode(name);
		formView.setName(tableAlias);
		formView.setRemark(remark);
		List<TableColumn> columns=tableColumnDao.getTableColumnByDataTableId(dataTable.getId());
		Integer columnAmount=getTemplateColumnAmount(dataTable.getId());
		String html=packagingFormHtml(columns,name,columnAmount);
		formView.setHtml(html);
		formView.setStandard(true);
		formView.setMenuId(menuId);
		formView.setFormState(DataState.ENABLE);
		formViewManager.saveFormView(formView, menuId,null,html);
		listViewManager.createDefaultListView(dataTable, name, tableAlias, remark, menuId);
	}
	
	private Integer getTemplateColumnAmount(Long dataTableId){
		Integer columnAmount=1;
		GenerateSetting generateSetting=generateSettingDao.getGenerateSettingByTable(dataTableId);
		if(generateSetting!=null&&generateSetting.getTemplateEnum()!=null){
			switch (generateSetting.getTemplateEnum()) {
			case TWO_COLUMN:
				columnAmount=1;//2列，1个字段
				break;
			case FOUR_COLUMN:
				columnAmount=2;//4列，2个字段
				break;
			case SIX_COLUMN:
				columnAmount=3;//6列，3个字段
				break;
			}
		}
		return columnAmount;
	}
	
	private String packagingFormHtml(List<TableColumn> columns,String name,Integer columnAmount) {
		if(columns==null||columns.isEmpty()){
			return "";
		}
		StringBuilder html=new StringBuilder();
		html.append("<table class=\"form-table-border-left\"  border=\"1\">");
		html.append("<tbody>");
		html.append(createColumnTable(columns,name,columnAmount));
		html.append("</tbody>");
		html.append("</table>");
		html.append("<br />");
		return html.toString();
	}
	
	private String createColumnTable(List<TableColumn> columns,String name,Integer columnAmount){
		StringBuilder html=new StringBuilder();
		int total=columns.size();
		int remainder=total%columnAmount;
		int i=0;
		for(TableColumn col:columns){
			if(i%columnAmount==0){
				html.append("<tr>");
			}
			html.append("<td>").append(getInternation(col.getAlias())).append("</td>");
			html.append("<td>").append(getControlHtml(col,name)).append("</td>");
			if((i+1)%columnAmount==0){
				html.append("</tr>");
			}
			if((i+1)==total && remainder>0){
				html.append("<td colspan=\""+(columnAmount-remainder)*2+"\" style=\"border-top:1px\"></td>");
				html.append("</tr>");
			}
			i++;
		}
		return html.toString();
	}
	
	private String getControlHtml(TableColumn col,String name){
		if(DataType.TEXT.equals(col.getDataType())||DataType.ENUM.equals(col.getDataType())){//文本、枚举
			return getTextControlHtml(col,"TEXT");
		}else if(DataType.DATE.equals(col.getDataType())){//日期
			return getDateControlHtml(col,"DATE");
		}else if(DataType.TIME.equals(col.getDataType())){//时间
			return getDateControlHtml(col,"TIME");
		}else if(DataType.INTEGER.equals(col.getDataType())||DataType.NUMBER.equals(col.getDataType())){//整型、数字
			return getTextControlHtml(col,"INTEGER");
		}else if(DataType.LONG.equals(col.getDataType())){//长整型
			return getTextControlHtml(col,"LONG");
		}else if(DataType.DOUBLE.equals(col.getDataType())||DataType.FLOAT.equals(col.getDataType())||DataType.AMOUNT.equals(col.getDataType())){//浮点数、金额
			return getTextControlHtml(col,"DOUBLE");
		}else if(DataType.BOOLEAN.equals(col.getDataType())){//布尔型
			return getRadioControlHtml(col);
		}else if(DataType.CLOB.equals(col.getDataType())){//大文本
			return getTextareaControlHtml(col);
		}else if(DataType.COLLECTION.equals(col.getDataType())){//集合
			return getCollectionControlHtml(col,name);
		}
		return "";
	}
	
	private String getCollectionControlHtml(TableColumn col,String dataTableName) {
		StringBuilder html=new StringBuilder();
//		html.append(getInternation(col.getAlias())).append(":");
		html.append("<input");
		html.append(" id=\"").append(col.getName()).append("\"");
		html.append(" title=\"").append(getInternation(col.getAlias())).append("\"");
		html.append(" value=\"").append(getInternation(col.getAlias())).append("\"");
		html.append(" type=\"button\" datatype=\"COLLECTION\" plugintype=\"STANDARD_LIST_CONTROL\" dbname=\"\"");
		html.append(" name=\"").append(col.getName()).append("\"");
		html.append(" columnid=\"").append(col.getId()).append("\"");
		html.append(" listviewcode=\"").append(dataTableName).append("\"");//listviewcode默认为dataTableName
		html.append("/>");
		return html.toString();
	}
	private String getRadioControlHtml(TableColumn col) {
		StringBuilder html=new StringBuilder();
//		html.append(getInternation(col.getAlias())).append(":");
		html.append("<input");
		html.append(" name=\"").append(col.getName()).append("\"");
		html.append(" title=\"").append(col.getAlias()).append("\"");
		html.append(" datatype=\"BOOLEAN\" plugintype=\"RADIO\" selectvalues=\"是;true,否;false\" initselectvalue=\"\" type=\"RADIO\"/>");
		return html.toString();
	}
	
	private String getTextControlHtml(TableColumn col,String datatype) {
		StringBuilder html=new StringBuilder();
//		html.append(getInternation(col.getAlias())).append(":");
		html.append("<input");
		html.append(" id=\"").append(col.getName()).append("\"");
		html.append(" title=\"").append(getInternation(col.getAlias())).append("\"");
		if(StringUtils.isNotEmpty(col.getDefaultValue())){
			html.append(" value=\"").append(col.getDefaultValue()).append("\"");
		}
		if(col.getMaxLength()!=null){
			html.append(" maxlength=\"").append(col.getMaxLength()).append("\"");
		}
		html.append(" name=\"").append(col.getName()).append("\"");
		html.append(" datatype=\"").append(datatype).append("\"");
		html.append(" format=\"number\" request=\"false\" signaturevisible=\"false\" formattip=\"数字\" readolny=\"false\" formattype=\"null\" plugintype=\"TEXT\"");
		html.append("/>");
		return html.toString();
	}
	
	private String getTextareaControlHtml(TableColumn col) {
		StringBuilder html=new StringBuilder();
//		html.append(getInternation(col.getAlias())).append(":");
		html.append("<textarea");
		html.append(" id=\"").append(col.getName()).append("\"");
		html.append(" title=\"").append(getInternation(col.getAlias())).append("\"");
		html.append(" name=\"").append(col.getName()).append("\"");
		html.append(" dataType=\"").append(col.getDataType()).append("\"");
		if(col.getMaxLength()==null){
			html.append(" maxlength=\"\"");
		}else{
			html.append(" maxlength=\"").append(col.getMaxLength()).append("\"");
			html.append(" onkeyup=\"calTextareaLen(value,").append(col.getMaxLength()).append(",this);\" ");
		}
		html.append(" style=\"width:354px;height:139px;\"  plugintype=\"textarea\" ");
		html.append(" defaultvalue=\"").append(col.getDefaultValue()==null?"":col.getDefaultValue()).append("\"");
		html.append(">");
		html.append(col.getDefaultValue()==null?"":col.getDefaultValue());
		html.append("</textarea> ");
		return html.toString();
	}
	private String getDateControlHtml(TableColumn col,String datatype) {
		StringBuilder html=new StringBuilder();
//		html.append(getInternation(col.getAlias())).append(":");
		html.append("<input");
		html.append(" id=\"").append(col.getName()).append("\"");
		html.append(" title=\"").append(getInternation(col.getAlias())).append("\"");
		if(StringUtils.isNotEmpty(col.getDefaultValue())){
			html.append(" value=\"").append(col.getDefaultValue()).append("\"");
		}
		html.append(" datatype=\"").append(datatype).append("\"");
		if("DATE".equals(datatype)){
			html.append(" format=\"yyyy-MM-dd\"");
		}else{
			html.append(" format=\"yyyy-MM-dd HH:mm\"");
		}
		html.append(" request=\"false\"  plugintype=\"TIME\" readonly=\"readonly\" ");
		html.append(" name=\"").append(col.getName()).append("\"");
		html.append("/>");
		return html.toString();
	}

	@Transactional(readOnly = false)
	public String changeTableState(List<Long> tableIds, Long menuId){
		int draftToEn=0,enToDis=0,disToEn=0;
		StringBuilder sbu=new StringBuilder("");
		for(Long tableId:tableIds){
			DataTable dataTable = getDataTable(tableId);
			if (dataTable.getTableState().equals(DataState.DRAFT)) {// 草稿->启用
				log.debug("table state has change to " + DataState.ENABLE.toString());
				dataTable.setTableState(DataState.ENABLE);
				draftToEn++;
				log.debug("begin to create defaultView");
				createDefaultView(dataTable, menuId);
			} else if (dataTable.getTableState().equals(DataState.ENABLE)) {// 启用->禁用
				log.debug("table state has change to " + DataState.DISABLE.toString());
				dataTable.setTableState(DataState.DISABLE);
				enToDis++;
			} else if (dataTable.getTableState().equals(DataState.DISABLE)) {// 禁用->启用
				log.debug("table state has change to " + DataState.ENABLE.toString());
				dataTable.setTableState(DataState.ENABLE);
				disToEn++;
			}
			saveDataTable(dataTable);
		}
		sbu.append(draftToEn).append(Struts2Utils.getText("formManager.draftToStart"))
		.append(enToDis).append(Struts2Utils.getText("formManager.startToForbidden"))
		.append(disToEn).append(Struts2Utils.getText("formManager.forbiddenToStart"));
		return sbu.toString();
	}

	/**
	 * 查询标准数据表(以createDate排序)
	 * 
	 * @param tables
	 */
	public List<DataTable> getStandardDataTables() {
		return dataTableDao.getStandardDataTables();
	}
	/**
	 * 查询自定义数据表(以createDate排序)
	 * 
	 * @param tables
	 */
	public List<DataTable> getDefaultDataTables() {
		return dataTableDao.getDefaultDataTables();
	}
	/**
	 * 查询某个系统下所有的数据表(以createDate排序)
	 * 
	 * @param tables
	 */
	public void getSystemAllDataTables(Page<DataTable> tables, Long menuId) {
		dataTableDao.findSystemAllDataTable(tables, menuId);
	}
	/**
	 * 查询某个系统下不包含自己的所有数据表(以createDate排序)
	 * 
	 * @param tables
	 */
	public void getSystemAllDataTables(Page<DataTable> tables, Long menuId,Long id) {
		dataTableDao.findSystemAllDataTable(tables, menuId,id);
	}
	/**
	 * 查询某个系统下所有自定义的数据表(以createDate排序)
	 * 
	 * @param tables
	 */
	public void getSystemDefaultDataTables(Page<DataTable> tables, Long menuId) {
		dataTableDao.findSystemDefaultDataTable(tables, menuId);
	}
	/**
	 * 通过表名查询数据表实体
	 * 
	 * @param tableName
	 * @return 返回是否验证成功
	 */
	public DataTable getDataTableByTableName(String tableName) {
		return  dataTableDao.findDataTableByName(tableName);
	}
	/**
	 * 获得该菜单中所有的数据表（包括自定义的数据表）
	 * @return
	 */
	public List<DataTable> getAllDataTablesByMenu(Long menuId){
		return dataTableDao.getAllDataTablesByMenu(menuId);
	}
	public List<DataTable> getUnCompanyAllDataTablesByMenu(Long menuId){
		return dataTableDao.getUnCompanyAllDataTablesByMenu(menuId);
	}
	/**
	 * 获得所有启用的标准数据表集合
	 * @param menuId
	 * @return
	 */
	public List<DataTable> getEnabledStandardDataTableByMenuId(Long menuId){
		return dataTableDao.getEnabledStandardDataTableByMenuId(menuId);
	}
	/**
	 * 根据menuId获得状态为草稿或启用的未删除的数据表
	 * @param menuId
	 * @return
	 */
	public List<DataTable> getDraftOrEnabledDataTableByMenuId(Long menuId){
		return dataTableDao.getDraftOrEnabledDataTableByMenuId(menuId);
	}
	
	 public String getInternation(String code){
		 return ApiFactory.getSettingService().getText(code);
	 }
	 public GenerateSetting getGenerateSettingByTable(Long tableId){
		 return generateSettingDao.getGenerateSettingByTable(tableId);
	 }
	 public GenerateSetting getGenerateSetting(Long settingId){
		 return generateSettingDao.getGenerateSetting(settingId);
	 }
	 public void saveGenerateSetting(GenerateSetting generateSetting){
		 generateSettingDao.save(generateSetting);
	 }
	 public Map<String,Object> generateService(DataTable dataTable, boolean processFlag) throws IOException {
			Map<String,Object> dataModel= new HashMap<String,Object>();
			String packageName = GenerateCodeUtils.getLastLayerPath(dataTable.getEntityName());
			String entityName = dataTable.getEntityName().substring(dataTable.getEntityName().lastIndexOf(".")+1,dataTable.getEntityName().length());
			dataModel.put("packageName", packageName);//包名
			dataModel.put("entityPath", dataTable.getEntityName());//实体引入路径
			dataModel.put("entityName", entityName);//实体名
			dataModel.put("lowCaseEntityName", GenerateCodeUtils.firstCharLowerCase(entityName));//小写实体名
			dataModel.put("processFlag", processFlag+"");
			
			dataModel.put("filePath", packageName.replaceAll("\\.", "/")+"/service/");
			dataModel.put("fileName", entityName+"Manager.java");
			dataModel.put("templateName", "generateService.ftl");
			return dataModel;
		}
		
	 public Map<String,Object> generateDao(DataTable dataTable) throws IOException {
		Map<String,Object> dataModel= new HashMap<String,Object>();
		String packageName = GenerateCodeUtils.getLastLayerPath(dataTable.getEntityName());
		String entityName = dataTable.getEntityName().substring(dataTable.getEntityName().lastIndexOf(".")+1,dataTable.getEntityName().length());
		dataModel.put("packageName", packageName);//包名
		dataModel.put("entityPath", dataTable.getEntityName());//实体引入路径
		dataModel.put("entityName", entityName);//实体名
		dataModel.put("lowCaseEntityName", GenerateCodeUtils.firstCharLowerCase(entityName));//小写实体名
		
		dataModel.put("filePath", packageName.replaceAll("\\.", "/")+"/dao/");
		dataModel.put("fileName", entityName+"Dao.java");
		dataModel.put("templateName", "generateDao.ftl");
		return dataModel;
	}
	 public Map<String, Object> generateAction(DataTable dataTable, boolean processFlag,Long menuId,String workflowCode,String logFlag)throws Exception{
		Map<String, Object> root = new HashMap<String, Object>();
		String className=dataTable.getEntityName();
		String entityName=className.substring(className.lastIndexOf(".")+1,className.length());
		String entityPath=className.substring(0,className.lastIndexOf("."));
		String packagePath=entityPath.substring(0,entityPath.lastIndexOf("."));
		String packageName=packagePath+".web";
		String namespace=packagePath.substring(packagePath.lastIndexOf(".")+1,packagePath.length());
		List<String> imports=new ArrayList<String>();
		imports.add(className);
		imports.add(packagePath+".service."+entityName+"Manager");
		String entityAttribute=GenerateCodeUtils.firstCharLowerCase(entityName);
		root.put("packageName", packageName);//包名
		root.put("namespace", namespace);
		root.put("entityName", entityName);//实体名
		root.put("entityAttribute", entityAttribute);//小写实体名
		root.put("imports", imports);
		root.put("containWorkflow", processFlag);
		root.put("workflowCode", workflowCode);
		root.put("ctx", "${ctx}");
		root.put("filePath", packageName.replaceAll("\\.", "/")+"/");
		root.put("fileName", entityName+"Action.java");
		root.put("logFlag", logFlag);
		root.put("entityAlias", dataTable.getAlias());
		root.put("templateName", "generateAction.ftl");
		addFunctions(namespace,processFlag,entityAttribute,menuId,dataTable);
		return root;
	}
	
	private void addFunctions(String namespace,boolean processFlag,String entityAttribute,Long menuId,DataTable dataTable) {
		Menu menu=menuDao.getMenu(menuId);
		BusinessSystem businessSystem= businessSystemManager.getBusiness(menu.getSystemId());
		if(processFlag){
			saveFunctions(namespace,entityAttribute,dataTable.getAlias()+"/放弃领取任务","abandonReceive",businessSystem);
			saveFunctions(namespace,entityAttribute,dataTable.getAlias()+"/加签","addSigner",businessSystem);
			saveFunctions(namespace,entityAttribute,dataTable.getAlias()+"/完成交互任务","completeInteractiveTask",businessSystem);
			saveFunctions(namespace,entityAttribute,dataTable.getAlias()+"/完成任务","completeTask",businessSystem);
			saveFunctions(namespace,entityAttribute,dataTable.getAlias()+"/领取任务","drawTask",businessSystem);
			saveFunctions(namespace,entityAttribute,dataTable.getAlias()+"/填写意见","fillOpinion",businessSystem);
			saveFunctions(namespace,entityAttribute,dataTable.getAlias()+"/流程监控中应急处理功能","processEmergency",businessSystem);
			saveFunctions(namespace,entityAttribute,dataTable.getAlias()+"/减签","removeSigner",businessSystem);
			saveFunctions(namespace,entityAttribute,dataTable.getAlias()+"/取回任务","retrieveTask",businessSystem);
			saveFunctions(namespace,entityAttribute,dataTable.getAlias()+"/显示流转历史","showHistory",businessSystem);
			saveFunctions(namespace,entityAttribute,dataTable.getAlias()+"/启动并提交流程","submitProcess",businessSystem);
			saveFunctions(namespace,entityAttribute,dataTable.getAlias()+"/办理任务页面","task",businessSystem);
			saveFunctions(namespace,entityAttribute,dataTable.getAlias()+"/抄送","copyTask",businessSystem);
			saveFunctions(namespace,entityAttribute,dataTable.getAlias()+"/退回任务","goback",businessSystem);
			saveFunctions(namespace,entityAttribute,dataTable.getAlias()+"/减签树","cutsignTree",businessSystem);
			saveFunctions(namespace,entityAttribute,dataTable.getAlias()+"/指派","assign",businessSystem);
		}
		saveFunctions(namespace,entityAttribute,dataTable.getAlias()+"/查看表单","view",businessSystem);
		saveFunctions(namespace,entityAttribute,dataTable.getAlias()+"/删除","delete",businessSystem);
		saveFunctions(namespace,entityAttribute,dataTable.getAlias()+"/新建页面","input",businessSystem);
		saveFunctions(namespace,entityAttribute,dataTable.getAlias()+"/列表页面","list",businessSystem);
		saveFunctions(namespace,entityAttribute,dataTable.getAlias()+"/可编辑列表页面","listEditable",businessSystem);
		saveFunctions(namespace,entityAttribute,dataTable.getAlias()+"/列表数据","listDatas",businessSystem);
		saveFunctions(namespace,entityAttribute,dataTable.getAlias()+"/保存","save",businessSystem);
		saveFunctions(namespace,entityAttribute,dataTable.getAlias()+"/编辑-保存","editSave",businessSystem);
		saveFunctions(namespace,entityAttribute,dataTable.getAlias()+"/编辑-删除","editDelete",businessSystem);
	}
	
	private void saveFunctions(String namespace,String entityAttribute,String functionName,String actionFunctionName,BusinessSystem businessSystem){
		String systemByCode=businessSystem.getCode();
		
		String functionPath="/"+namespace+"/"+entityAttribute+"-"+actionFunctionName+".htm";
		String code=systemByCode+"-"+namespace+"-"+entityAttribute+"-"+actionFunctionName;
		Function function=functionManager.getFunctionByPath(functionPath, businessSystem.getId());
		if(function==null){
			function=new Function();
			function.setCode(code);
			function.setName(functionName);
		}
		function.setPath(functionPath);
		function.setBusinessSystem(businessSystem);
		
		functionManager.saveFunction(function);
	}
	
	public Map<String, Object> generateList(DataTable dataTable, boolean processFlag,Long menuId,String workflowCode,boolean popupable,String menuChangeType)throws Exception{
		Map<String, Object> root = new HashMap<String, Object>();
		String className=dataTable.getEntityName();
		String entityName=className.substring(className.lastIndexOf(".")+1,className.length());
		String entityPath=className.substring(0,className.lastIndexOf("."));
		String packagePath=entityPath.substring(0,entityPath.lastIndexOf("."));
		String namespace=packagePath.substring(packagePath.lastIndexOf(".")+1,packagePath.length());
		List<String> imports=new ArrayList<String>();
		imports.add(className);
		imports.add(packagePath+".service."+entityName+"Manager");
		String entityAttribute=GenerateCodeUtils.firstCharLowerCase(entityName);
		root.put("entityName", entityName);//实体名
		root.put("entityAttribute", entityAttribute);//小写实体名
		root.put("entityAlias", dataTable.getAlias());//实体中文名
		root.put("namespace", namespace);
		root.put("containWorkflow", processFlag);
		root.put("workflowCode", workflowCode);
		root.put("listCode", dataTable.getName());
		root.put("ctx", "${ctx}");
		root.put("resourcesCtx", "${resourcesCtx}");
		root.put("imatrixCtx", "${imatrixCtx}");
		root.put("filePath", "jsp/"+namespace+"/");
		root.put("fileName", entityAttribute+"-list.jsp");
		root.put("templateName", "generateList.ftl");
		root.put("popupable", popupable);
		root.put("menuChangeType", menuChangeType);
		return root;
	}
	public Map<String, Object> generateInput(DataTable dataTable, boolean processFlag,Long menuId,String workflowCode,boolean popupable)throws Exception{
		Map<String, Object> root = new HashMap<String, Object>();
		String className=dataTable.getEntityName();
		String entityName=className.substring(className.lastIndexOf(".")+1,className.length());
		String entityPath=className.substring(0,className.lastIndexOf("."));
		String packagePath=entityPath.substring(0,entityPath.lastIndexOf("."));
		String namespace=packagePath.substring(packagePath.lastIndexOf(".")+1,packagePath.length());
		List<String> imports=new ArrayList<String>();
		imports.add(className);
		imports.add(packagePath+".service."+entityName+"Manager");
		String entityAttribute=GenerateCodeUtils.firstCharLowerCase(entityName);
		root.put("entityName", entityName);//实体名
		root.put("entityAttribute", entityAttribute);//小写实体名
		root.put("namespace", namespace);
		root.put("containWorkflow", processFlag);
		root.put("workflowCode", workflowCode);
		root.put("formCode", dataTable.getName());
		root.put("ctx", "${ctx}");
		root.put("id", "${id}");
		root.put("fieldPermission", "${fieldPermission}");
		root.put("resourcesCtx", "${resourcesCtx}");
		root.put("submitResult", "${submitResult}");
		root.put("entityObject", "${"+entityAttribute+"}");
		root.put("filePath", "jsp/"+namespace+"/");
		root.put("fileName", entityAttribute+"-input.jsp");
		root.put("templateName", "generateInput.ftl");
		root.put("taskId", "${taskId }");
		root.put("taskTransact", "${taskTransact }");
		root.put("popupable", popupable);
		root.put("autoFillOpinionInfo", "${autoFillOpinionInfo}");
		return root;
	}

	/**
	 * 生成实体代码
	 * @param dataTable
	 * @param processFlag2 
	 * @throws IOException
	 */
	@SuppressWarnings("deprecation")
	public  Map<String,Object> generateEntity(DataTable dataTable, boolean processFlag,boolean isParent) throws IOException {
			Map<String,Object> dataModel= new HashMap<String,Object>();
			Set<String> importList = new HashSet<String>();//需要引用类的集合
			List<String> attrList = new ArrayList<String>();//类属性集合
			List<String> methodList = new ArrayList<String>();//get和set方法集合
			//获取数据库类型（不同数据库大字段和大文本生成代码不同）
			String dataBase = PropUtils.getDataBase();
			String clob="", blob="";
			if(PropUtils.DATABASE_MYSQL.equals(dataBase)){
				clob="LONGTEXT";
				blob="LONGBLOB";
			}else if(PropUtils.DATABASE_ORACLE.equals(dataBase)){
				clob="CLOB";
				blob="BLOB";
			}else if(PropUtils.DATABASE_SQLSERVER.equals(dataBase)){
				clob="NTEXT";
				blob="image";
			}
			String packageName = dataTable.getEntityName().substring(0,dataTable.getEntityName().lastIndexOf("."));
			String entityName = dataTable.getEntityName().substring(dataTable.getEntityName().lastIndexOf(".")+1,dataTable.getEntityName().length());
			String entityAttribute=GenerateCodeUtils.firstCharLowerCase(entityName);//实体名第一个字母小写
			List<TableColumn> columns=tableColumnDao.getTableColumnByDataTableId(dataTable.getId());
			boolean haveImplant=false;//是否含有嵌入 true表示含有嵌入、false表示不含有嵌入
			for (TableColumn column : columns) {
				if(!isIdEntityField(column.getName())){//是否是IdEntity中的属性，如果不是才会生成代码
					if(column.getCasual()) importList.add("import javax.persistence.Transient;");
					String attrName = column.getName();
					String methodName = GenerateCodeUtils.firstCharUpperCase(attrName);//方法中大写的变量名
					if(attrName.contains(".")) continue;
					switch (column.getDataType()) {
					case TEXT:
						attrList.add("String_"+attrName+"_"+column.getCasual()+"_"+column.getAlias());//数据类型_属性名称_是否在数据库中生成_数据别名
						methodList.add("String_"+methodName+"_"+attrName);//数据类型_属性名（第一个字母大写）_属性名
						break;
					case DATE:
						attrList.add("Date_"+attrName+"_"+column.getCasual()+"_"+column.getAlias());
						importList.add("import java.util.Date;");
						methodList.add("Date_"+methodName+"_"+attrName);
						break;
					case TIME:
						attrList.add("Date_"+attrName+"_"+column.getCasual()+"_"+column.getAlias());
						importList.add("import java.util.Date;");
						methodList.add("Date_"+methodName+"_"+attrName);
						break;
					case INTEGER:
						attrList.add("Integer_"+attrName+"_"+column.getCasual()+"_"+column.getAlias());
						methodList.add("Integer_"+methodName+"_"+attrName);
						break;
					case LONG:
						attrList.add("Long_"+attrName+"_"+column.getCasual()+"_"+column.getAlias());
						methodList.add("Long_"+methodName+"_"+attrName);
						break;
					case DOUBLE:
						attrList.add("Double_"+attrName+"_"+column.getCasual()+"_"+column.getAlias());
						methodList.add("Double_"+methodName+"_"+attrName);
						break;
					case FLOAT:
						attrList.add("Float_"+attrName+"_"+column.getCasual()+"_"+column.getAlias());
						methodList.add("Float_"+methodName+"_"+attrName);
						break;
					case BOOLEAN:
						attrList.add("Boolean_"+attrName+"_"+column.getCasual()+"_"+column.getAlias());
						methodList.add("Boolean_"+methodName+"_"+attrName);
						break;
					case CLOB:
						attrList.add("CLOB_"+attrName+"_"+column.getCasual()+"_"+column.getAlias());
						importList.add("import javax.persistence.Lob;");
						importList.add("import javax.persistence.Column;");
						
						methodList.add("String_"+methodName+"_"+attrName);
						break;
					case BLOB:
						attrList.add("BLOB_"+attrName+"_"+column.getCasual()+"_"+column.getAlias());
						importList.add("import javax.persistence.Lob;");
						importList.add("import javax.persistence.Column;");
						importList.add("import javax.persistence.Basic;");
						importList.add("import javax.persistence.FetchType;");
						methodList.add("byte[]_"+methodName+"_"+attrName);
						break;
					case COLLECTION:
						if(StringUtils.isNotEmpty(column.getObjectPath())){
							String collectionName = column.getObjectPath();
							if(collectionName.contains(".")){
								collectionName = column.getObjectPath().substring(column.getObjectPath().lastIndexOf(".")+1,column.getObjectPath().length());
							}
							attrList.add("COLLECTION_List<"+collectionName+"> "+attrName+"_"+column.getCasual()+"_"+column.getAlias());
							methodList.add("List<"+collectionName+">"+"_"+methodName+"_"+attrName);
							importList.add("import java.util.List;");
						}
						break;
					case ENUM:
						if(StringUtils.isNotEmpty(column.getObjectPath())){
							String enumName = column.getObjectPath();
							if(enumName.contains(".")){
								enumName = column.getObjectPath().substring(column.getObjectPath().lastIndexOf(".")+1,column.getObjectPath().length());
							}
							attrList.add(enumName+"_"+attrName+"_"+column.getCasual()+"_"+column.getAlias());
							methodList.add(enumName+"_"+methodName+"_"+attrName);
						}
						break;
					case REFERENCE:
						if(StringUtils.isNotEmpty(column.getObjectPath())){
							String refName = column.getObjectPath();
							if(refName.contains(".")){
								refName = column.getObjectPath().substring(column.getObjectPath().lastIndexOf(".")+1,column.getObjectPath().length());
							}
							if(RelationSetting.ONE_TO_ONE.equals(column.getRelationSetting())){
								attrList.add("ONETOONE_"+refName+" "+attrName+"_"+column.getCasual()+"_"+column.getAlias());
							}else if(RelationSetting.MANY_TO_ONE.equals(column.getRelationSetting())){
								attrList.add("MANYTOONE_"+refName+" "+attrName+"_"+column.getCasual()+"_"+column.getAlias());
							}else if(RelationSetting.IMPLANT.equals(column.getRelationSetting())){
								attrList.add("IMPLANT_"+refName+" "+attrName+"_"+column.getCasual()+"_"+column.getAlias());
								haveImplant=true;
							}
							methodList.add(refName+"_"+methodName+"_"+attrName);
						}
						break;
					case AMOUNT:
						attrList.add("Float_"+attrName+"_"+column.getCasual()+"_"+column.getAlias());
						methodList.add("Float_"+methodName+"_"+attrName);
						break;
					case NUMBER:
						attrList.add("Integer_"+attrName+"_"+column.getCasual()+"_"+column.getAlias());
						methodList.add("Integer_"+methodName+"_"+attrName);
						break;
					default:
						break;
					}
					if(StringUtils.isNotEmpty(column.getObjectPath())){
						String path = column.getObjectPath();
						if(path.contains(".")){
							importList.add("import "+path+";");
						}
					}
				}
			}
			if(processFlag){
				importList.add("import com.norteksoft.wf.engine.client.ExtendField;");
				importList.add("import com.norteksoft.wf.engine.client.FormFlowable;");
				importList.add("import com.norteksoft.wf.engine.client.WorkflowInfo;");
				importList.add("import javax.persistence.Embedded;");
			}else if(haveImplant){
				importList.add("import javax.persistence.Embedded;");
			}
			dataModel.put("packageName", packageName);//包名
			dataModel.put("tableName", dataTable.getName());//数据表名
			dataModel.put("entityName", entityName);//实体名
			dataModel.put("importList", importList);
			dataModel.put("attrList", attrList);
			dataModel.put("methodList", methodList);
			dataModel.put("clob", clob);
			dataModel.put("blob", blob);
			dataModel.put("processFlag", processFlag+"");
			
			dataModel.put("filePath", packageName.replaceAll("\\.", "/")+"/");
			dataModel.put("fileName", entityName+".java");
			dataModel.put("templateName", "generateEntity.ftl");
			
			dataModel.put("entityAttribute", entityAttribute);
			dataModel.put("entityAlias", dataTable.getAlias());
			if(isParent){
				dataModel.put("isParent", "true");
			}else{
				dataModel.put("isParent", "false");
			}
			return dataModel;
	}

	/**
	 * 生成可编辑list列表模板
	 * @param dataTable
	 * @param processFlag
	 * @return
	 */
	public Map<String, Object> generateEditableList(DataTable dataTable,boolean processFlag) {
		Map<String,Object> dataModel= new HashMap<String,Object>();
		String packageName = GenerateCodeUtils.getLastLayerPath(dataTable.getEntityName());
		String entityName = dataTable.getEntityName().substring(dataTable.getEntityName().lastIndexOf(".")+1,dataTable.getEntityName().length());
		String nameSpace = packageName.substring(packageName.lastIndexOf(".")+1,packageName.length());
		String lowCaseEntityName = GenerateCodeUtils.firstCharLowerCase(entityName);
		dataModel.put("processFlag", processFlag+"");
		dataModel.put("fileName", lowCaseEntityName+"-listEditable.jsp");
		dataModel.put("templateName", "generateEditableList.ftl");
		dataModel.put("entityName", entityName);//实体名
		dataModel.put("lowCaseEntityName", lowCaseEntityName);
		dataModel.put("ctx", "${ctx}");
		dataModel.put("resourcesCtx", "${resourcesCtx}");
		dataModel.put("nameSpace", nameSpace);
		dataModel.put("listCode", dataTable.getName());
		dataModel.put("filePath", "jsp/"+nameSpace);
		return dataModel;
	}

	public Map<String, Object> generateTask(DataTable dataTable,Boolean processFlag,Boolean popupable) {
		Map<String,Object> dataModel= new HashMap<String,Object>();
		String packageName = GenerateCodeUtils.getLastLayerPath(dataTable.getEntityName());
		String entityName = dataTable.getEntityName().substring(dataTable.getEntityName().lastIndexOf(".")+1,dataTable.getEntityName().length());
		String nameSpace = packageName.substring(packageName.lastIndexOf(".")+1,packageName.length());
		String lowCaseEntityName = GenerateCodeUtils.firstCharLowerCase(entityName);
		dataModel.put("containWorkflow", processFlag);
		dataModel.put("fileName", lowCaseEntityName+"-task.jsp");
		dataModel.put("templateName", "generateTask.ftl");
		dataModel.put("lowCaseEntityName",lowCaseEntityName );//实体名
		dataModel.put("ctx", "${ctx}");
		dataModel.put("fieldPermission", "${fieldPermission}");
		dataModel.put("taskId", "${taskId}");
		dataModel.put("companyId", "${"+lowCaseEntityName+".companyId}");
		dataModel.put("id", "${id}");
		dataModel.put("resourcesCtx", "${resourcesCtx}");
		dataModel.put("imatrixCtx", "${imatrixCtx}");
		dataModel.put("nameSpace", nameSpace);
		dataModel.put("formCode", dataTable.getName());
		dataModel.put("filePath", "jsp/"+nameSpace);
		dataModel.put("entity", "${"+lowCaseEntityName+"}");
		dataModel.put("popupable", popupable);
		dataModel.put("autoFillOpinionInfo", "${autoFillOpinionInfo}");
		return dataModel;
	}
	
	public void loadActionClass(String root,String systemCode,MyClassLoader myClassLoader){
		   try{
			   Long systemId = null;
			   File dir=new File(root);
			   if(dir.exists()){
				   File[] files=dir.listFiles();
				   if(files!=null){
					   createFunction(files,myClassLoader,systemId,systemCode); 
				   }
			   }
		   }catch (Exception e) {
//			   logger.debug(" loadActionClass Exception : "+e.getMessage()); 
			   e.printStackTrace();
		   }
	   }
	   
	   private void createFunction(File[] files,MyClassLoader myClassLoader,Long systemId,String systemCode) throws Exception{
		   BusinessSystem businessSystem= businessSystemManager.getSystemBySystemCode(systemCode);
		   for(int i=0;i<files.length;i++){
			   File filei = files[i];
			   if(filei.isDirectory()){
				   File[] fileis=filei.listFiles();
				   if(fileis!=null){
					   createFunction(fileis,myClassLoader,systemId,systemCode); 
				   }
			   }else{
				   String filename = filei.getName();
				   if(filename.indexOf(".")>=0){
					   String filetype = filename.substring(filename.lastIndexOf(".")+1);
					   String fileName = filename.substring(0,filename.lastIndexOf("."));
					   String filePath = filei.getPath();
					   if("class".equals(filetype)){//表示是class类
						   if(fileName.endsWith("Action")){//表示是action类
							   
							   com.norteksoft.mms.base.autoTool.Util.loadFile(filei,myClassLoader);
							   Class clazz =myClassLoader.loadClass(Util.getClassName(filePath));
							   Annotation[] actionsAnos = clazz.getAnnotations();
							   String nameSpace = "";
							   String codePart = "";
							   for(Annotation n:actionsAnos){
								   if("org.apache.struts2.convention.annotation.Namespace".equals(n.annotationType().getName())){//表示是@Namespace注解
									   nameSpace = (String)n.getClass().getMethod("value").invoke(n);//获得命名空间
									   codePart = nameSpace.replace("/", "-");//将/替换为-
									   break;
								   }
							   }
							   if(StringUtils.isEmpty(nameSpace)){//表示没有@Namespace注解,则命名空间已action名称命名
								   String parentPath = filei.getParent();
								   nameSpace = "/"+parentPath.substring(parentPath.lastIndexOf("\\")+1);
							   }
							   
							   String functionCode = null;
							   String functionPath = null;
							   Method[] methods = clazz.getMethods();
							   for(Method method:methods){
								   Annotation[] nns = method.getAnnotations();
								   Function function = null;
								   String actionValue = null;
								   boolean isAction = false;
								   Annotation metaAnno = null;
								   for(Annotation n:nns){
									   if("org.apache.struts2.convention.annotation.Action".equals(n.annotationType().getName())){//表示是@Action注解
										   actionValue = (String)n.getClass().getMethod("value").invoke(n);
										   
										   function = new Function();
										   
										   if(StringUtils.isNotEmpty(systemCode)){//工具中“系统编码”不为空
											   functionCode = systemCode+codePart+"-"+actionValue;//系统编码_命名空间_action注解的值
											   function.setCode(functionCode);
										   }
										   
										   function.setName(functionCode);
										   functionPath = nameSpace+"/"+actionValue+".htm";// /命名空间/action注解的值.htm
										   function.setPath(functionPath);
										   function.setBusinessSystem(businessSystem);
										   
										   isAction = true;
									   }
									   if("com.norteksoft.product.web.struts2.MetaData".equals(n.annotationType().getName())){//表示有自定义的注解@MetaData
										   metaAnno = n;
									   }
								   }
								   if(isAction){//表示有@Action注解，则生成function资源
									   Function funInfo = null;
									   if(metaAnno!=null){
										   funInfo = getFunctionInfo(function,metaAnno,codePart,businessSystem,fileName,actionValue);
									   }else{
										  funInfo = functionManager.getFunctionByPath(function.getPath(), businessSystem.getId());//判断资源是否存在
										   if(funInfo==null){
											   funInfo = function;
										   }else{
											   copyFunction(function,funInfo);//重新设置资源编码、路径、系统
										   }
									   }
									   if(funInfo!=null){
										   //插入资源
										   functionManager.saveFunction(funInfo);
									   }
								   }
							   }
						   }
						   
					   }
				   }
			   }
		   }
	   }
	   
	   
	   private Function getFunctionInfo(Function function,Annotation metaData,String codePart,BusinessSystem businessSystem, String ActionClassName,String actionValue) throws Exception{
		   boolean isAuth = (Boolean)metaData.getClass().getMethod("isAuth").invoke(metaData);
		   boolean isMenu = (Boolean)metaData.getClass().getMethod("isMenu").invoke(metaData);
		   String describe = (String)metaData.getClass().getMethod("describe").invoke(metaData);
		   Function funInfo = null;
		   
		   if(businessSystem!=null){
			   String funPath = function.getPath();
			   funInfo = functionManager.getFunctionByPath(funPath, businessSystem.getId());//判断资源是否存在
			   if(funInfo==null){
				   funInfo = function;
			   }else{
				   copyFunction(function,funInfo);//重新设置资源编码、路径、系统
			   }
			   if(!isAuth){
				   funInfo.setCode("IS_AUTHENTICATED_ANONYMOUSLY");
				   funInfo.setFunctionType(FunctionType.DEFAULT);
			   }else{
				   if(funInfo.getId()==null){//如果资源不存在则更新资源编码
					   if(StringUtils.isNotEmpty(actionValue)){
						   String functionCode = businessSystem.getCode()+codePart+"-"+actionValue;//系统编码_命名空间_action注解的值
						   funInfo.setCode(functionCode);
					   }
				   }
			   }
			   funInfo.setBusinessSystem(businessSystem);
			   funInfo.setIsmenu(isMenu);
			   funInfo.setName(describe);
		   }
		   return funInfo;
	   }
	   
	   /**
	    * 重新设置资源编码、路径、系统
	    * @param srcFun
	    * @param destFun
	    */
	   private void copyFunction(Function srcFun,Function destFun){
		   destFun.setCode(srcFun.getCode());
		   destFun.setPath(srcFun.getPath());
		   destFun.setBusinessSystem(srcFun.getBusinessSystem());
	   }
	   
	   /**
	    * 解析加载数据表
	    * @param packagePath
	    * @param database
	    * @param stmt
	    * @param companyId
	    * @param menuId
	    */
	   public  void loadDataTableClass(Long companyId,Long menuId,MyClassLoader myClassLoader,String root) {
		   try{
			   File dir=new File(root);
			   if(dir.exists()){
				   File[] files=dir.listFiles();
				   if(files!=null){
					   createDataTable(files,myClassLoader,companyId,menuId); 
				   }
			   }
		   }catch (Exception e) {
//			   logger.debug(" loadDataTableClass Exception : "+e.getMessage()); 
			   e.printStackTrace();
		   }
	   }
	   
	   private void createDataTable(File[] files,MyClassLoader myClassLoader,Long companyId,Long menuId) throws Exception{
		   for(int i=0;i<files.length;i++){
			   File filei = files[i];
			   if(filei.isDirectory()){
				   File[] fileis=filei.listFiles();
				   if(fileis!=null){
					   createDataTable(fileis,myClassLoader,companyId,menuId); 
				   }
			   }else{
				   String filename = filei.getName();
				   String filetype = filename.substring(filename.lastIndexOf(".")+1);
				   String filePath = filei.getPath();
				   if("class".equals(filetype)){//表示是class类
					   String entityName = filePath.substring(Util.classPath.length()+1,filePath.lastIndexOf("."));//com/norteksoft/bs/holiday/entity/DataType
					   entityName = entityName.replace("/", ".").replace("\\", ".");//com.norteksoft.bs.holiday.entity.DataType
					   
					   Util.loadFile(filei,myClassLoader);
					   Class clazz =myClassLoader.loadClass(Util.getClassName(filePath));
					   
					   DataTable dataTable = null;
					   String tableName = null;
					   Annotation[] nns = clazz.getAnnotations();
					   boolean isEntity = false;
					   for(Annotation n:nns){
						   if("javax.persistence.Entity".equals(n.annotationType().getName())){//表示是实体
							   isEntity = true;
							   break;
						   }
					   }
					   if(isEntity){//表示是实体
						   for(Annotation n:nns){
							   if("javax.persistence.Table".equals(n.annotationType().getName())){//表示配置了表名
								   javax.persistence.Table t = (javax.persistence.Table)n;
								   //获得数据表
								   tableName = t.name();
								   dataTable = getDataTableByTableName(tableName);//判断数据表是否存在
								   if(dataTable==null){
									   dataTable = new DataTable();
								   }
								   dataTable.setName(tableName);
								   if(dataTable.getId()==null){
									   dataTable.setTableState(DataState.DRAFT);
								   }
								   dataTable.setEntityName(entityName);
								   dataTable.setCompanyId(companyId);
								   dataTable.setMenuId(menuId);
								   
								   for(Annotation mn:nns){
									   if("com.norteksoft.product.web.struts2.MetaData".equals(mn.annotationType().getName())){//表示有自定义的注解@MetaData
										   String describe = (String)mn.getClass().getMethod("describe").invoke(mn);
										   if(StringUtils.isNotEmpty(describe)&&StringUtils.isEmpty(dataTable.getAlias()))dataTable.setAlias(describe);//如果注解中设置数据表名且数据表别名字段为空
										   dataTableDao.save(dataTable);
										   break;
									   }
								   }
								   if(StringUtils.isEmpty(dataTable.getAlias())&&StringUtils.isNotEmpty(tableName))dataTable.setAlias(tableName);//如果注解中没有设置数据表名且数据表别名字段为空
								   //插入数据表DataTable
								   dataTableDao.save(dataTable);
								   Long dataTableId = dataTable.getId();
								   //创建当前类中包含的字段
								   createTableColumn(clazz,dataTableId,companyId);
								   createGenerateSetting(dataTable.getId());
								   break;
							   }
						   }
						  
					   }
				   }
			   }
		   }
	   }
	   /**
	    * 生成“代码配置”，不生成实体
	    * @param tableId
	    */
	   private void createGenerateSetting(Long tableId){
		   GenerateSetting generateSetting = new GenerateSetting();
		   generateSetting.setEntitative(false);
		   generateSetting.setTableId(tableId);
		   generateSettingDao.save(generateSetting);
	   }
	   
	   private void createTableColumn(Class clazz,Long dataTableId,Long companyId) throws Exception{
		   Field[] fs = clazz.getDeclaredFields();//获得实体对应的所有属性
		   int j=0;
		   for(Field f:fs){
			   Annotation[]  fnns = f.getAnnotations();//获得字段的注解集合
			   if(!"serialVersionUID".equals(f.getName())){
				   TableColumn tableColumn = tableColumnDao.getTableColumnByColName(dataTableId, f.getName());//判断数据表字段是否存在
				   if(tableColumn==null){
					   tableColumn = new TableColumn();
				   }
				   tableColumn.setName(f.getName());
				   tableColumn.setDbColumnName(Util.analysisFieldName(f.getName()));
				   if(tableColumn.getDisplayOrder()==null)tableColumn.setDisplayOrder(j);
				   tableColumn.setDataTableId(dataTableId);
				   tableColumn.setCompanyId(companyId);
				   if(tableColumn.getDataType()==null||(tableColumn.getDataType()!=null&&(tableColumn.getDataType()!=DataType.TIME&&tableColumn.getDataType()!=DataType.DATE))){
					   tableColumn.setDataType(getDataType(f.getType().getName()));
				   }
				   
				   for(Annotation fn:fnns){
					   String fnTypeName = fn.annotationType().getName();
					   if("javax.persistence.Embedded".equals(fnTypeName)){
						   tableColumn = null;
						   break;
					   }
					   AnnotationAnalysis annotationAnalysis = DataTableAnnotationFactory.getAnnotationAnalysis(fn);
					   if(annotationAnalysis!=null)annotationAnalysis.getTableColumn(fn, tableColumn, f);
				   }
				   if(tableColumn!=null&&StringUtils.isEmpty(tableColumn.getAlias()))tableColumn.setAlias(f.getName());//如果注解中没有设置字段别名名且字段别名字段为空
				   if(tableColumn!=null){//当是嵌入式Embedded时，返回的字段为空
					   Class ftClass = f.getType();
					   if(ftClass.isEnum()){//表示是枚举
						   tableColumn.setDataType(DataType.ENUM);
						   tableColumn.setObjectPath(ftClass.getName());//获得枚举类路径
					   }
					   //插入数据表字段
					   tableColumnDao.save(tableColumn);
				   }
			   }
			   j++;
		   }
	   }
	   /**
	    * 是否是com.norteksoft.product.orm.IdEntity中的字段。
	    * 如果是则在生成代码时，不生成到相应的实体中，因为实体都继承了该IdEntity，如果该实体再生成IdEntity中的字段，启动服务时会报异常。
	    * @param fieldName
	    * @return
	    */
	   private boolean isIdEntityField(String fieldName){
		   if("id".equals(fieldName)||"companyId".equals(fieldName)||"creator".equals(fieldName)||"creatorName".equals(fieldName)||"createdTime".equals(fieldName)||"modifier".equals(fieldName)
				   ||"modifierName".equals(fieldName)||"modifiedTime".equals(fieldName)||"departmentId".equals(fieldName)||"creatorId".equals(fieldName)||"subCompanyId".equals(fieldName)){
			   return true;
		   }
		   return false;
	   }
	   
	   private DataType getDataType(String type){
		   if("java.lang.Long".equals(type)){
			  return DataType.LONG;
		   }else if("java.lang.String".equals(type)){
			   return DataType.TEXT;
		   }else if("java.lang.Float".equals(type)){
			   return DataType.FLOAT;
		   }else if("java.lang.Integer".equals(type)){
			   return DataType.INTEGER;
		   }else if("java.lang.Double".equals(type)){
			   return DataType.DOUBLE;
		   }else if("java.lang.Boolean".equals(type)){
			   return DataType.BOOLEAN;
		   }else if("java.util.Date".equals(type)){
			   return DataType.DATE;
		   }
		   return null;

	   }
}
