package com.travudget.travudget

import android.annotation.SuppressLint
import android.content.Intent
import android.widget.TextView
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.HorizontalBarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import java.util.Date
import java.util.Calendar
import com.github.mikephil.charting.charts.BarChart
import java.io.Serializable
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.text.SimpleDateFormat
import java.util.Locale

class Informes : AppCompatActivity() {

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.informes)

        val btnReturn = findViewById<ImageButton>(R.id.btn_return)
        btnReturn.setOnClickListener {
            val viatgeId = intent.getStringExtra("viatgeId")
            val emailCreador = intent.getStringExtra("emailCreador")

            Thread.sleep(500)
            val intent = Intent(this, Viatge::class.java).apply {
                putExtra("viatgeId", viatgeId)
                putExtra("emailCreador", emailCreador)
            }
            startActivity(intent)
            finish()
        }

        //Gràfic 1
        val chart = findViewById<HorizontalBarChart >(R.id.chart)

        val despesaTotal = intent.getIntExtra("despesaTotal", 0)
        val pressupostTotal = intent.getIntExtra("pressupostTotal", 0)

        val barValue = if (pressupostTotal != 0) {
            (despesaTotal.toFloat() / pressupostTotal.toFloat()) * 100
        } else {
            0f
        }

        val entries = ArrayList<BarEntry>()
        entries.add(BarEntry(0f, barValue))

        val dataSet = BarDataSet(entries, barValue.toString())
        if (barValue < 100f) dataSet.color = Color.rgb(0, 155, 0)
        else dataSet.color = Color.rgb(155, 0, 0)

        val barData = BarData(dataSet)
        chart.data = barData

        chart.xAxis.isEnabled = false
        chart.axisLeft.isEnabled = false
        chart.axisRight.isEnabled = false

        chart.axisLeft.axisMinimum = 0f
        chart.axisLeft.axisMaximum = 100f

        chart.description.isEnabled = false
        chart.legend.isEnabled = false

        chart.barData.isHighlightEnabled = false


        val textView = findViewById<TextView>(R.id.textPercentatge)
        val text = "$barValue% del pressupost gastat ($despesaTotal de $pressupostTotal)"
        textView.text = text

        chart.invalidate()

        //Gràfic 2
        val pressupostVariableSerializable = intent.getSerializableExtra("pressupostVariable")
        val pressupostVariable = (pressupostVariableSerializable as HashMap<String, Int>).toMutableMap()
        val despesaPerDiaSerializable = intent.getSerializableExtra("despesaPerDia")
        val despesaPerDiaNotFormat = (despesaPerDiaSerializable as HashMap<String, Int>).toMutableMap()

        val despesaPerDia = despesaPerDiaNotFormat.mapKeys { (key, _) ->
            formatDate(key)
        }.toMutableMap()

        val despesaPerDiaSorted = despesaPerDiaNotFormat
            .mapKeys { formatDate(it.key) }
            .toSortedMap(compareBy { it })

        val entries2 = ArrayList<BarEntry>()
        despesaPerDiaSorted.forEach { (day, expense) ->
            val budget = pressupostVariable[day] ?: 0
            val color = if (expense < budget) Color.GREEN else Color.RED
            entries2.add(BarEntry(entries2.size.toFloat(), expense.toFloat(), day))
        }

        val dataSet2 = BarDataSet(entries2, "Despesa per Dia")
        dataSet2.valueTextSize = 14f

        val colors = entries2.map { entry ->
            val budget = pressupostVariable[entry.data as String] ?: 0
            if (entry.y < budget) Color.GREEN else Color.RED
        }.toIntArray()
        dataSet2.colors = colors.toList()

        val chart2 = findViewById<BarChart>(R.id.chart2)
        chart2.setDrawValueAboveBar(true)
        chart2.description.isEnabled = false
        chart2.legend.isEnabled = false

        val xAxis = chart2.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawLabels(true)
        xAxis.setDrawAxisLine(true)
        xAxis.setDrawGridLines(false)
        val labels = despesaPerDiaSorted.keys.toTypedArray()
        xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        xAxis.granularity = 1f

        val rightAxis = chart2.axisRight
        rightAxis.isEnabled = false
        val yAxis = chart2.axisLeft
        yAxis.axisMinimum = 0f
        yAxis.setDrawLabels(false)
        yAxis.setDrawAxisLine(false)

        val barData2 = BarData(dataSet2)
        chart2.data = barData2
        chart2.barData.barWidth = 0.5f
        chart2.invalidate()

        //Gràfic 3
        val despeses = intent.getSerializableExtra("despeses") as ArrayList<Serializable>

        val despesesList = ArrayList<DespesaShowInfo>()
        for (despesa in despeses) {
            if (despesa is DespesaShowInfo) {
                despesesList.add(despesa)
            }
        }

        val despesaPerCategoria = HashMap<String, Float>()
        for (despesa in despesesList) {
            val categoria = despesa.categoria
            val preu = despesa.preu.toFloat()
            if (despesaPerCategoria.containsKey(categoria)) {
                despesaPerCategoria[categoria] = despesaPerCategoria[categoria]!! + preu
            } else {
                despesaPerCategoria[categoria] = preu
            }
        }

        val pieChart = findViewById<PieChart>(R.id.pieChart)

        val entries3 = ArrayList<PieEntry>()
        for ((categoria, despesa) in despesaPerCategoria) {
            entries3.add(PieEntry(despesa, categoria))
        }

        val dataSet3 = PieDataSet(entries3, "Despesa per categoria")
        dataSet3.setColors(ColorTemplate.MATERIAL_COLORS.asList())
        val pieData = PieData(dataSet3)

        pieChart.data = pieData
        pieChart.description.isEnabled = false
        pieChart.legend.isEnabled = false

        pieChart.setEntryLabelTextSize(14f)
        dataSet3.setValueTextSize(14f)
        dataSet3.setValueTextColor(Color.BLACK)

        pieChart.invalidate()

        val layoutDeutes = findViewById<LinearLayout>(R.id.layoutDeutes)
        val deutesSerializable = intent.getSerializableExtra("deutes")
        val deutes = (deutesSerializable as HashMap<String, Int>).toMutableMap()
        println("DEUTES: $deutes")

        val llistaDeutes = HashMap<String, Int>()

        for ((clau, valor) in deutes) {
            val (deutor, rep, _, _) = clau.split("/")
            val deutorNoEmail = deutor.replace("@gmail.com", "")
            val repNoEmail = rep.replace("@gmail.com", "")
            val clauDeute = "$deutorNoEmail -> $repNoEmail"

            if (llistaDeutes.containsKey(clauDeute)) {
                val deuteExistent = llistaDeutes[clauDeute] ?: 0
                llistaDeutes[clauDeute] = deuteExistent + valor
            } else {
                llistaDeutes[clauDeute] = valor
            }
        }

        for ((clau, total) in llistaDeutes) {
            val textView = TextView(this)
            textView.text = "$clau $total"
            layoutDeutes.addView(textView)
        }

    }

    private fun formatDate(data: String): String {
        val currentFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH)

        val desiredFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        val date = currentFormat.parse(data)

        return desiredFormat.format(date)    }
}
