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
import com.ruoyi.kms.domain.SystemMetadata;
import com.ruoyi.kms.service.ISystemMetadataService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 系统元数据Controller
 * 
 * @author hby
 * @date 2025-09-28
 */
@RestController
@RequestMapping("/kms/systemMetadata")
public class SystemMetadataController extends BaseController
{
    @Autowired
    private ISystemMetadataService systemMetadataService;

    /**
     * 查询系统元数据列表
     */
    @PreAuthorize("@ss.hasPermi('kms:systemMetadata:list')")
    @GetMapping("/list")
    public TableDataInfo list(SystemMetadata systemMetadata)
    {
        startPage();
        List<SystemMetadata> list = systemMetadataService.selectSystemMetadataList(systemMetadata);
        return getDataTable(list);
    }

    /**
     * 导出系统元数据列表
     */
    @PreAuthorize("@ss.hasPermi('kms:systemMetadata:export')")
    @Log(title = "系统元数据", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, SystemMetadata systemMetadata)
    {
        List<SystemMetadata> list = systemMetadataService.selectSystemMetadataList(systemMetadata);
        ExcelUtil<SystemMetadata> util = new ExcelUtil<SystemMetadata>(SystemMetadata.class);
        util.exportExcel(response, list, "系统元数据数据");
    }

    /**
     * 获取系统元数据详细信息
     */
    @PreAuthorize("@ss.hasPermi('kms:systemMetadata:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(systemMetadataService.selectSystemMetadataById(id));
    }

    /**
     * 新增系统元数据
     */
    @PreAuthorize("@ss.hasPermi('kms:systemMetadata:add')")
    @Log(title = "系统元数据", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody SystemMetadata systemMetadata)
    {
        return toAjax(systemMetadataService.insertSystemMetadata(systemMetadata));
    }

    /**
     * 修改系统元数据
     */
    @PreAuthorize("@ss.hasPermi('kms:systemMetadata:edit')")
    @Log(title = "系统元数据", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody SystemMetadata systemMetadata)
    {
        return toAjax(systemMetadataService.updateSystemMetadata(systemMetadata));
    }

    /**
     * 删除系统元数据
     */
    @PreAuthorize("@ss.hasPermi('kms:systemMetadata:remove')")
    @Log(title = "系统元数据", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(systemMetadataService.deleteSystemMetadataByIds(ids));
    }
}
