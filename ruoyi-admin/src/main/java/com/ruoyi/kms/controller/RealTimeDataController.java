package com.ruoyi.kms.controller;

import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.kms.domain.RealTimeData;
import com.ruoyi.kms.service.IRealTimeDataService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 关键指标实时数据Controller
 * 
 * @author hby
 * @date 2025-09-28
 */
@RestController
@RequestMapping("/kms/realTimeData")
public class RealTimeDataController extends BaseController
{
    @Autowired
    private IRealTimeDataService realTimeDataService;

    /**
     * 查询关键指标实时数据列表
     */
    @PreAuthorize("@ss.hasPermi('kms:realTimeData:list')")
    @GetMapping("/list")
    public TableDataInfo list(RealTimeData realTimeData)
    {
        startPage();
        List<RealTimeData> list = realTimeDataService.selectRealTimeDataList(realTimeData);
        return getDataTable(list);
    }

    /**
     * 导出关键指标实时数据列表
     */
    @PreAuthorize("@ss.hasPermi('kms:realTimeData:export')")
    @Log(title = "关键指标实时数据", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, RealTimeData realTimeData)
    {
        List<RealTimeData> list = realTimeDataService.selectRealTimeDataList(realTimeData);
        ExcelUtil<RealTimeData> util = new ExcelUtil<RealTimeData>(RealTimeData.class);
        util.exportExcel(response, list, "关键指标实时数据数据");
    }

    /**
     * 获取关键指标实时数据详细信息
     */
    @PreAuthorize("@ss.hasPermi('kms:realTimeData:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(realTimeDataService.selectRealTimeDataById(id));
    }

    /**
     * 新增关键指标实时数据
     */
    @PreAuthorize("@ss.hasPermi('kms:realTimeData:add')")
    @Log(title = "关键指标实时数据", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody RealTimeData realTimeData)
    {
        return toAjax(realTimeDataService.insertRealTimeData(realTimeData));
    }

    /**
     * 修改关键指标实时数据
     */
    @PreAuthorize("@ss.hasPermi('kms:realTimeData:edit')")
    @Log(title = "关键指标实时数据", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody RealTimeData realTimeData)
    {
        return toAjax(realTimeDataService.updateRealTimeData(realTimeData));
    }

    /**
     * 删除关键指标实时数据
     */
    @PreAuthorize("@ss.hasPermi('kms:realTimeData:remove')")
    @Log(title = "关键指标实时数据", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(realTimeDataService.deleteRealTimeDataByIds(ids));
    }
}
