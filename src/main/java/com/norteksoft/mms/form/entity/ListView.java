package com.norteksoft.mms.form.entity;

import java.io.Serializable;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;

import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import com.norteksoft.mms.form.enumeration.OrderType;
import com.norteksoft.mms.form.enumeration.StartQuery;
import com.norteksoft.mms.form.enumeration.TotalType;
import com.norteksoft.product.orm.IdEntityNoExtendField;
/**
 * 列表视图
 * @author wurong
 *
 */
@Entity
@Table(name="MMS_LIST_VIEW")
public class ListView extends IdEntityNoExtendField  implements Serializable, Cloneable{

	private static final long serialVersionUID = 1L;
	@OneToMany(cascade=CascadeType.ALL,mappedBy="listView")
	@LazyCollection(LazyCollectionOption.TRUE)
	@OrderBy("displayOrder asc")
	private List<ListColumn> columns;//列设置
	
	private Boolean rowNumbers = false;//是否显示序号
	private Boolean pagination = true;//是否分页
	private Boolean totalable = true;//是否显示列表总条数
	private Boolean searchTotalable = true;//查询时是否显示列表总条数
	private Integer rowNum;//默认行数
	@Column(length=100)
	private String rowList;//可选行数
	
	private Boolean defaultListView = false;//是否默认
	private Boolean editable;//是否需要操作:增改
	private Integer actWidth=90;//操作列宽设置
	@Column(length=150)
	private String editUrl;//表格中编辑时保存url
	private Boolean advancedQuery=false;//是否高级查询
	private StartQuery startQuery=StartQuery.INSIDE_QUERY;//是否启用查询
	private Boolean popUp=false;//是否是弹出式查询(true为弹出，false为嵌入)
	@Column(length=150)
	private String deleteUrl;//删除的url
	@Column(length=50)
	private String orderFieldName;//行顺序字段名称(如果该字段为空表示不能行拖到)
	@Column(length=150)
	private String dragRowUrl;//行拖动后保存顺序的url
	private Boolean multiSelect=true;//是否可以多选
	@Column(length=50)
	private String defaultSortField;//默认排序字段
	@Enumerated(EnumType.STRING)
	private OrderType orderType = OrderType.DESC;//默认排序方式
	private Integer frozenColumn;//冻结列数
	@Enumerated(EnumType.STRING)
	private TotalType totalType = TotalType.CURRENT_PAGE;//合计方式
	
	private Boolean multiboxSelectOnly=true;//仅点击复选框时选中
	@OneToMany(mappedBy="listView",cascade=CascadeType.ALL)
	@LazyCollection(LazyCollectionOption.TRUE)
	private List<JqGridProperty> jqGridPropertys;//属性自由扩展列表
	
	private Boolean searchFaint=true;//是否启用模糊查询
	@Column(length=50)
	private String code;//编码
	@Column(length=100)
	private String name;//名称
	@ManyToOne
	@JoinColumn(name="FK_DATA_TABLE_ID")
	private DataTable dataTable;//数据表
	
	@Column(length=200)
	private String remark;//备注

	private Boolean standard=false;//是否是标准的视图
	@Column(name="FK_MENU_ID")
	private Long menuId;//菜单列表
	
	private Boolean deleted=false;//是否已删除
	
	public Boolean getDefaultListView() {
		return defaultListView;
	}
	public void setDefaultListView(Boolean defaultListView) {
		this.defaultListView = defaultListView;
	}
	public List<ListColumn> getColumns() {
		return columns;
	}
	public void setColumns(List<ListColumn> columns) {
		this.columns = columns;
	}
	public Integer getRowNum() {
		return rowNum;
	}
	public void setRowNum(Integer rowNum) {
		this.rowNum = rowNum;
	}
	public String getRowList() {
		return rowList;
	}
	public void setRowList(String rowList) {
		this.rowList = rowList;
	}
	
	public Boolean getEditable() {
		return editable;
	}
	public void setEditable(Boolean editable) {
		this.editable = editable;
	}
	public String getEditUrl() {
		return editUrl;
	}
	public void setEditUrl(String editUrl) {
		this.editUrl = editUrl;
	}
	public Boolean getPagination() {
		return pagination;
	}
	public void setPagination(Boolean pagination) {
		this.pagination = pagination;
	}
	public String getCustomProperty() {
		String customProperty="";
		if(jqGridPropertys!=null){
			for(Object obj:jqGridPropertys){
				JqGridProperty jqGridProperty=(JqGridProperty)obj;
				if(StringUtils.isNotEmpty(jqGridProperty.getName())&&StringUtils.isNotEmpty(jqGridProperty.getValue())){
					if(StringUtils.isNotEmpty(customProperty)){
						customProperty+=",";
					}
					customProperty+=jqGridProperty.getName()+":"+jqGridProperty.getValue();
				}
			}
		}
		return customProperty;
	}
	public Boolean getAdvancedQuery() {
		return advancedQuery;
	}
	public void setAdvancedQuery(Boolean advancedQuery) {
		this.advancedQuery = advancedQuery;
	}
	public String getDeleteUrl() {
		return deleteUrl;
	}
	public void setDeleteUrl(String deleteUrl) {
		this.deleteUrl = deleteUrl;
	}
	public String getOrderFieldName() {
		return orderFieldName;
	}
	public void setOrderFieldName(String orderFieldName) {
		this.orderFieldName = orderFieldName;
	}
	public String getDragRowUrl() {
		return dragRowUrl;
	}
	public void setDragRowUrl(String dragRowUrl) {
		this.dragRowUrl = dragRowUrl;
	}
	public Boolean getMultiSelect() {
		return multiSelect;
	}
	public void setMultiSelect(Boolean multiSelect) {
		this.multiSelect = multiSelect;
	}
	public Boolean getMultiboxSelectOnly() {
		return multiboxSelectOnly;
	}
	public void setMultiboxSelectOnly(Boolean multiboxSelectOnly) {
		this.multiboxSelectOnly = multiboxSelectOnly;
	}
	public List<JqGridProperty> getJqGridPropertys() {
		return jqGridPropertys;
	}
	public void setJqGridPropertys(List<JqGridProperty> jqGridPropertys) {
		this.jqGridPropertys = jqGridPropertys;
	}
	public Boolean getPopUp() {
		return popUp;
	}
	public void setPopUp(Boolean popUp) {
		this.popUp = popUp;
	}
	public String getDefaultSortField() {
		return defaultSortField;
	}
	public void setDefaultSortField(String defaultSortField) {
		this.defaultSortField = defaultSortField;
	}
	public OrderType getOrderType() {
		return orderType;
	}
	public void setOrderType(OrderType orderType) {
		this.orderType = orderType;
	}
	public Integer getFrozenColumn() {
		return frozenColumn;
	}
	public void setFrozenColumn(Integer frozenColumn) {
		this.frozenColumn = frozenColumn;
	}
	public Boolean getRowNumbers() {
		return rowNumbers;
	}
	public void setRowNumbers(Boolean rowNumbers) {
		this.rowNumbers = rowNumbers;
	}
	public StartQuery getStartQuery() {
		return startQuery;
	}
	public void setStartQuery(StartQuery startQuery) {
		this.startQuery = startQuery;
	}
	public TotalType getTotalType() {
		return totalType;
	}
	public void setTotalType(TotalType totalType) {
		this.totalType = totalType;
	}
	public Integer getActWidth() {
		return actWidth;
	}
	public void setActWidth(Integer actWidth) {
		this.actWidth = actWidth;
	}
	public Boolean getTotalable() {
		return totalable;
	}
	public void setTotalable(Boolean totalable) {
		this.totalable = totalable;
	}
	public Boolean getSearchTotalable() {
		return searchTotalable;
	}
	public void setSearchTotalable(Boolean searchTotalable) {
		this.searchTotalable = searchTotalable;
	}
	public Boolean getSearchFaint() {
		return searchFaint;
	}
	public void setSearchFaint(Boolean searchFaint) {
		this.searchFaint = searchFaint;
	}
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public DataTable getDataTable() {
		return dataTable;
	}

	public void setDataTable(DataTable dataTable) {
		this.dataTable = dataTable;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}
	public Boolean getStandard() {
		return standard;
	}

	public void setStandard(Boolean standard) {
		this.standard = standard;
	}

	public Long getMenuId() {
		return menuId;
	}

	public void setMenuId(Long menuId) {
		this.menuId = menuId;
	}

	public Boolean getDeleted() {
		return deleted;
	}

	public void setDeleted(Boolean deleted) {
		this.deleted = deleted;
	}
	@Override
	public ListView clone(){
		try {
			return (ListView) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			throw new RuntimeException("ListView clone failure");
		}
	}
}
