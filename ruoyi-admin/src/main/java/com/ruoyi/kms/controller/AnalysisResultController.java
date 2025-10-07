package com.ruoyi.kms.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import com.ruoyi.kms.domain.AnalysisResult;
import com.ruoyi.kms.service.IAnalysisResultService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 预警结果，关联实时数据，主删除从同步删除Controller
 * 
 * @author hby
 * @date 2025-09-27
 */
@RestController
@RequestMapping("/kms/analysisResult")
public class AnalysisResultController extends BaseController
{
    @Autowired
    private IAnalysisResultService analysisResultService;

    /**
     * 查询预警结果，关联实时数据，主删除从同步删除列表
     */
    @PreAuthorize("@ss.hasPermi('kms:analysisResult:list')")
    @GetMapping("/list")
    public TableDataInfo list(AnalysisResult analysisResult)
    {
        startPage();
        List<AnalysisResult> list = analysisResultService.selectAnalysisResultList(analysisResult);
        return getDataTable(list);
    }

    /**
     * 导出预警结果，关联实时数据，主删除从同步删除列表
     */
    @PreAuthorize("@ss.hasPermi('kms:analysisResult:export')")
    @Log(title = "预警结果，关联实时数据，主删除从同步删除", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, AnalysisResult analysisResult)
    {
        List<AnalysisResult> list = analysisResultService.selectAnalysisResultList(analysisResult);
        ExcelUtil<AnalysisResult> util = new ExcelUtil<AnalysisResult>(AnalysisResult.class);
        util.exportExcel(response, list, "预警结果，关联实时数据，主删除从同步删除数据");
    }
    /**
     * 获取预警统计数据
     * @return 统计结果
     */
    @GetMapping("/count")
    @PreAuthorize("@ss.hasPermi('kms:analysisResult:list')")
    public AjaxResult getWarningCount() {
        // 创建统计结果Map
        Map<String, Integer> countMap = new HashMap<>();

        // 统计总预警数 (isHandled = null表示查询全部)
        int total = analysisResultService.selectWarningCount(null);
        countMap.put("total", total);

        // 统计未处理预警数 (isHandled = 0)
        int unHandledCount = analysisResultService.selectWarningCount(0);
        countMap.put("unHandled", unHandledCount);

        // 统计已处理预警数 (isHandled = 1)
        int handledCount = analysisResultService.selectWarningCount(1);
        countMap.put("handled", handledCount);

        return AjaxResult.success(countMap);
    }
    /**
     * 获取预警结果，关联实时数据，主删除从同步删除详细信息
     */
    @PreAuthorize("@ss.hasPermi('kms:analysisResult:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(analysisResultService.selectAnalysisResultById(id));
    }

    /**
     * 新增预警结果，关联实时数据，主删除从同步删除
     */
    @PreAuthorize("@ss.hasPermi('kms:analysisResult:add')")
    @Log(title = "预警结果，关联实时数据，主删除从同步删除", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody AnalysisResult analysisResult)
    {
        return toAjax(analysisResultService.insertAnalysisResult(analysisResult));
    }

    /**
     * 修改预警结果，关联实时数据，主删除从同步删除
     */
    @PreAuthorize("@ss.hasPermi('kms:analysisResult:edit')")
    @Log(title = "预警结果，关联实时数据，主删除从同步删除", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody AnalysisResult analysisResult)
    {
        return toAjax(analysisResultService.updateAnalysisResult(analysisResult));
    }

    /**
     * 删除预警结果，关联实时数据，主删除从同步删除
     */
    @PreAuthorize("@ss.hasPermi('kms:analysisResult:remove')")
    @Log(title = "预警结果，关联实时数据，主删除从同步删除", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(analysisResultService.deleteAnalysisResultByIds(ids));
    }
    /**
     * 处理预警
     */
    @PostMapping("/handle")
    public AjaxResult handleWarning(@RequestBody AnalysisResult warning) {
        if (warning == null || warning.getId() == null) {
            return error("预警ID不能为空");
        }

        AnalysisResult exist = analysisResultService.selectAnalysisResultById(warning.getId());
        if (exist == null) {
            return error("预警记录不存在");
        }

        exist.setIsHandled(1);
        analysisResultService.updateAnalysisResult(exist);
        return success();
    }

}
