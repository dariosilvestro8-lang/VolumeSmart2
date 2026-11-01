
package it.volumesmart.app

import android.app.Activity
import android.app.AlertDialog
import android.app.TimePickerDialog
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.widget.*
import java.util.Locale

class MainActivity : Activity() {

    private lateinit var root: LinearLayout
    private lateinit var scheduleList: LinearLayout

    private val colors = intArrayOf(
        Color.rgb(115, 65, 220),
        Color.rgb(235, 65, 145),
        Color.rgb(20, 160, 190),
        Color.rgb(245, 125, 45)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showHome()
    }

    private fun showHome() {
        val themeIndex = getPreferences(0)
            .getInt("theme", 0)
            .coerceIn(0, colors.lastIndex)

        val selectedColor = colors[themeIndex]

        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 32, 24, 24)
            background = GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                intArrayOf(
                    Color.rgb(35, 15, 75),
                    selectedColor,
                    Color.rgb(25, 125, 175)
                )
            )
        }

        val scroll = ScrollView(this)
        scroll.addView(root)
        setContentView(scroll)

        root.addView(text("VOLUME SMART", 14, Color.WHITE, true))
        root.addView(text(
            "Il volume segue\nil tuo ritmo.",
            32, Color.WHITE, true
        ).apply {
            setPadding(0, 20, 0, 12)
        })

        root.addView(text(
            "Personalizza i tuoi orari e lascia fare tutto al telefono.",
            15, Color.WHITE, false
        ))

        root.addView(text(
            "LE TUE FASCE ORARIE",
            14, Color.WHITE, true
        ).apply {
            setPadding(0, 35, 0, 15)
        })

        scheduleList = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        root.addView(scheduleList)

        root.addView(makeButton(
            "+  AGGIUNGI FASCIA",
            Color.WHITE,
            selectedColor
        ) {
            editSchedule(null)
        })

        root.addView(text(
            "PERSONALIZZA IL TUO STILE",
            14, Color.WHITE, true
        ).apply {
            setPadding(0, 35, 0, 15)
        })

        val themeRow = LinearLayout(this)

        colors.forEachIndexed { index, color ->
            val swatch = TextView(this).apply {
                text = if (index == themeIndex) "●" else "○"
                textSize = 27f
                gravity = Gravity.CENTER
                setTextColor(color)
                setPadding(8, 8, 8, 8)

                setOnClickListener {
                    getPreferences(0).edit()
                        .putInt("theme", index)
                        .apply()
                    showHome()
                }
            }

            themeRow.addView(
                swatch,
                LinearLayout.LayoutParams(0, 65, 1f)
            )
        }

        root.addView(themeRow)

        refreshSchedules()
    }

    private fun refreshSchedules() {
        scheduleList.removeAllViews()

        val schedules = ScheduleStore.getSchedules(this)

        if (schedules.isEmpty()) {
            scheduleList.addView(text(
                "Non hai ancora creato una fascia oraria.",
                14, Color.WHITE, false
            ).apply {
                setPadding(15, 20, 15, 20)
            })
        }

        schedules.forEach { item ->

            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(18, 16, 18, 16)
                background = rounded(0x33FFFFFF, 22)
            }

            card.addView(text(
                item.name, 19, Color.WHITE, true
            ))

            card.addView(text(
                "${item.start}  →  ${item.end}",
                16, Color.WHITE, false
            ).apply {
                setPadding(0, 8, 0, 5)
            })

            card.addView(text(
                "Volume: ${item.volume}%",
                14, Color.WHITE, true
            ))

            val buttons = LinearLayout(this).apply {
                gravity = Gravity.END
            }

            val edit = TextView(this).apply {
                text = "MODIFICA"
                setTextColor(Color.WHITE)
                setPadding(12, 14, 12, 5)
                setOnClickListener {
                    editSchedule(item)
                }
            }

            val delete = TextView(this).apply {
                text = "ELIMINA"
                setTextColor(0xFFFFB7CF.toInt())
                setPadding(12, 14, 5, 5)

                setOnClickListener {
                    val updated = ScheduleStore.getSchedules(
                        this@MainActivity
                    ).filterNot { it.id == item.id }

                    ScheduleStore.saveSchedules(
                        this@MainActivity, updated
                    )

                    AlarmScheduler.scheduleAll(this@MainActivity)
                    refreshSchedules()
                }
            }

            buttons.addView(edit)
            buttons.addView(delete)
            card.addView(buttons)

            scheduleList.addView(
                card,
                LinearLayout.LayoutParams(-1, -2).apply {
                    bottomMargin = 14
                }
            )
        }
    }

    private fun editSchedule(existing: Schedule?) {

        val name = EditText(this).apply {
            hint = "Nome della fascia"
            setText(existing?.name ?: "")
        }

        var start = existing?.start ?: "08:00"
        var end = existing?.end ?: "09:00"

        val startButton = Button(this).apply {
            text = "Inizio: $start"
            setOnClickListener {
                chooseTime(start) {
                    start = it
                    text = "Inizio: $it"
                }
            }
        }

        val endButton = Button(this).apply {
            text = "Fine: $end"
            setOnClickListener {
                chooseTime(end) {
                    end = it
                    text = "Fine: $it"
                }
            }
        }

        val volume = SeekBar(this).apply {
            max = 100
            progress = existing?.volume ?: 50
        }

        val percentage = TextView(this).apply {
            text = "${volume.progress}%"
            textSize = 20f
            setTextColor(Color.BLACK)
        }

        volume.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    percentage.text = "$progress%"
                }

                override fun onStartTrackingTouch(
                    seekBar: SeekBar?
                ) {}

                override fun onStopTrackingTouch(
                    seekBar: SeekBar?
                ) {}
            }
        )

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 10, 20, 10)

            addView(name)
            addView(startButton)
            addView(endButton)

            addView(TextView(this@MainActivity).apply {
                text = "Volume desiderato"
                textSize = 15f
            })

            addView(percentage)
            addView(volume)
        }

        AlertDialog.Builder(this)
            .setTitle("Imposta fascia oraria")
            .setView(content)
            .setNegativeButton("Annulla", null)
            .setPositiveButton("SALVA") { _, _ ->

                val currentSchedules =
                    ScheduleStore.getSchedules(this)

                val id = existing?.id ?: (
                    (currentSchedules.maxOfOrNull {
                        it.id
                    } ?: 0) + 1
                )

                val newSchedule = Schedule(
                    id = id,
                    name = name.text.toString()
                        .trim()
                        .ifEmpty { "La mia fascia" },
                    start = start,
                    end = end,
                    volume = volume.progress,
                    enabled = true
                )

                val updated = currentSchedules
                    .filterNot { it.id == id }
                    .toMutableList()

                updated.add(newSchedule)

                ScheduleStore.saveSchedules(this, updated)
                AlarmScheduler.scheduleAll(this)
                refreshSchedules()
            }
            .show()
    }

    private fun chooseTime(
        current: String,
        callback: (String) -> Unit
    ) {
        val parts = current.split(":")

        TimePickerDialog(
            this,
            { _, hour, minute ->
                val time = String.format(
                    Locale.getDefault(),
                    "%02d:%02d",
                    hour,
                    minute
                )
                callback(time)
            },
            parts[0].toInt(),
            parts[1].toInt(),
            true
        ).show()
    }

    private fun text(
        value: String,
        size: Int,
        color: Int,
        bold: Boolean
    ) = TextView(this).apply {
        text = value
        textSize = size.toFloat()
        setTextColor(color)

        if (bold) {
            setTypeface(null, Typeface.BOLD)
        }
    }

    private fun makeButton(
        value: String,
        backgroundColor: Int,
        textColor: Int,
        action: () -> Unit
    ) = Button(this).apply {
        text = value
        setTextColor(textColor)
        background = rounded(backgroundColor, 22)
        setOnClickListener { action() }
    }

    private fun rounded(color: Int, radius: Int) =
        GradientDrawable().apply {
            setColor(color)
            cornerRadius = radius.toFloat()
        }
}
