package com.example.skeleton.helper

import android.content.Context
import android.graphics.Color
import com.example.skeleton.R
import com.example.skeleton.helper.ResourceHelper.dp
import com.example.skeleton.helper.UsageStatsHelper.HOUR_24
import com.example.skeleton.model.UsageDigest
import com.example.skeleton.model.UsageSummary
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.github.mikephil.charting.formatter.PercentFormatter
import java.io.File

@Suppress("MemberVisibilityCanBePrivate")
object GraphHelper {
    fun getCumulative(context: Context, filename: String): List<Entry> {
        val path = File(context.filesDir.path + "/" + filename)
        val records = CsvHelper.read(path).sortedBy { it.starTime }
        if (records.isEmpty()) {
            return listOf()
        }
        val l = hashMapOf<Float, Float>()
        var culDuration = 0L
        val startOfDay = (records.first().starTime / 3600 / 1000) * 3600000

        for (r in records) {
            l[(r.starTime - startOfDay) / 60000f] = culDuration / 3600000f
            culDuration += r.duration
            l[(r.starTime + r.duration - startOfDay) / 60000f] = culDuration / 3600000f
        }
        return l.toSortedMap().map { Entry(it.key, it.value) }
    }

    fun plotDailyLineChart(context: Context, day: String): LineChart? {
        val entries = getCumulative(context, day)
        if (entries.isEmpty()) {
            return null
        }
        val dataSet = LineDataSet(entries, "Usage Time")
        val lineData = LineData(dataSet)

        return LineChart(context).apply {
            data = lineData

            dataSet.apply {
                color = Color.parseColor("#00d0ff")
                setCircleColor(Color.parseColor("#00d0ff"))
                setDrawCircleHole(false)
                setDrawCircles(false)
                lineWidth = 3f
                fillDrawable = context.getDrawable(R.drawable.fade_background)
                setDrawFilled(true)
                fillAlpha
            }

            xAxis.apply {
                textSize = 13f
                position = XAxis.XAxisPosition.BOTTOM
                axisMinimum = 0f
                axisMaximum = HOUR_24 / 60000f
                labelRotationAngle = 30f
                axisLineWidth = 3f
                setLabelCount(5, true)
                valueFormatter = IAxisValueFormatter { value, _ -> String.format("%02d:%02d", value.toInt() / 60, value.toInt() % 60) }
            }

            axisLeft.apply {
                textSize = 13f
                axisMinimum = 0f
                setLabelCount(5, false)
                valueFormatter = IAxisValueFormatter { value, _ -> value.toInt().toString() + "h" }
                axisLineWidth = 3f
            }

            isDoubleTapToZoomEnabled = false
            axisRight.isEnabled = false
            isDragEnabled = false
            isScaleXEnabled = false
            isScaleYEnabled = false
            description.isEnabled = false
            legend.isEnabled = false
        }
    }

    fun get7DaySummary(context: Context, endTime: Long): List<BarEntry> {
        val series = arrayListOf<BarEntry>()
        var i = 1f
        for (t in (endTime - 6 * HOUR_24)..endTime step HOUR_24) {
            val d = UsageDigest.load(context, CalendarHelper.getDayCondensed(t))
            series.add(BarEntry(i, (d?.totalTime ?: 0) / 3600000f))
            i += 1
        }
        return series
    }

    fun plot7DayBarChart(context: Context, time: Long): BarChart? {
        val entries = get7DaySummary(context, time)
        if (entries.isEmpty()) {
            return null
        }
        val dataSet = BarDataSet(entries, "Usage Time")
        val barData = BarData(dataSet)

        return BarChart(context).apply {
            data = barData

            dataSet.apply {
                valueTextSize = 11f
                setValueFormatter { value, _, _, _ ->
                    if (value != 0f) String.format("%d:%02d", value.toInt(), ((value - value.toInt()) * 60).toInt()) else ""
                }
            }

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                textSize = 13f
                labelRotationAngle = 30f

                valueFormatter = IAxisValueFormatter { value, _ ->
                    val t = time - (7 - value.toLong()) * HOUR_24
                    CalendarHelper.getMonthDay(t)
                }
            }

            axisLeft.apply {
                axisMinimum = 0f
                setLabelCount(5, false)
                textSize = 13f
                valueFormatter = IAxisValueFormatter { value, _ -> value.toInt().toString() + "h" }
            }

            isDoubleTapToZoomEnabled = false
            axisRight.isEnabled = false
            isDragEnabled = true
            isScaleXEnabled = false
            isScaleYEnabled = false
            description.isEnabled = false
            legend.isEnabled = false
        }
    }

    fun plotSummaryPieChart(context: Context): PieChart {

        return PieChart(context).apply {
            setUsePercentValues(true)
            description.isEnabled = false
            legend.isEnabled = false

            setDrawCenterText(true)
            centerText = "Total Usage Time"
            setCenterTextColor(Color.parseColor("#2b83bd"))
            setCenterTextSize(16f)

            isDrawHoleEnabled = true
            setHoleColor(Color.WHITE)

            setTransparentCircleColor(Color.WHITE)
            setTransparentCircleAlpha(110)

            holeRadius = 55f
            transparentCircleRadius = 63f

            setEntryLabelColor(Color.WHITE)
            setEntryLabelTextSize(13f)
        }
    }

    fun setData(pieChart: PieChart, usageSummary: List<UsageSummary>) {
        val entries = arrayListOf<PieEntry>()
        val sum = usageSummary.map { it.useTimeTotal }.sum()
        var cul = 0L
        for (i in usageSummary) {
            if (cul > 0.85 * sum || i.useTimeTotal < 0.05 * sum) {
                entries.add(PieEntry((sum - cul).toFloat(), "Others"))
                break
            } else {
                cul += i.useTimeTotal
                entries.add(PieEntry(i.useTimeTotal.toFloat(), i.appName))
            }
        }
        val colors = arrayListOf(
                Color.parseColor("#99ff33"),
                Color.parseColor("#fff133"),
                Color.parseColor("#ffb133"),
                Color.parseColor("#33d3ff")
        )
        val pieDataSet = PieDataSet(entries, "Total Usage Time").apply {
            sliceSpace = dp(3).toFloat()
            selectionShift = dp(5).toFloat()
            setColors(colors)
        }
        val pieData = PieData(pieDataSet).apply {
            setValueFormatter(PercentFormatter())
            setValueTextSize(15f)
            setValueTextColor(Color.WHITE)
        }
        pieChart.data = pieData
        pieChart.invalidate()
        pieChart.animateY(1500, Easing.EasingOption.EaseInOutQuad)
    }
}