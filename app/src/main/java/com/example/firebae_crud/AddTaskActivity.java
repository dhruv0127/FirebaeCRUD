package com.example.firebae_crud;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.icu.util.TimeZone;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class AddTaskActivity extends AppCompatActivity {

//    TextView setDueDateTextView;
    Calendar calendar;
    Button saveButton;
    EditText taskEditText;

    MaterialButton setDueDateTextView ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_task);

//        setDueDateTextView = findViewById(R.id.set_due_tv);
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        saveButton = findViewById(R.id.save_btn);
        taskEditText = findViewById(R.id.task_edittext);
        setDueDateTextView  = findViewById(R.id.due_date_button);

        MaterialDatePicker<Long> materialDatePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Due Date")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build();

        setDueDateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                materialDatePicker.show(getSupportFragmentManager(), "MATERIAL_DATE_PICKER");
            }
        });

        materialDatePicker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener<Long>() {
            @Override
            public void onPositiveButtonClick(Long selection) {
                Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                calendar.setTimeInMillis(selection);
                SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy", Locale.US);
                String selectedDate = dateFormat.format(calendar.getTime());
            }
        });

        calendar = Calendar.getInstance();

        Intent intent = getIntent();
        if (intent.hasExtra("task") && intent.hasExtra("due") && intent.hasExtra("id")) {
            String taskText = intent.getStringExtra("task");
            String dueDate = intent.getStringExtra("due");
            String taskId = intent.getStringExtra("id");

            taskEditText.setText(taskText);
            setDueDateTextView.setText(dueDate);

            setDueDateTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int year = calendar.get(Calendar.YEAR);
                    int month = calendar.get(Calendar.MONTH);
                    int day = calendar.get(Calendar.DAY_OF_MONTH);

                    DatePickerDialog datePickerDialog = new DatePickerDialog(AddTaskActivity.this,
                            (view, selectedYear, selectedMonth, selectedDay) -> {
                                String dueDate = (selectedMonth + 1) + "-" + selectedDay + "-" + selectedYear;
                                setDueDateTextView.setText(dueDate);
                            }, year, month, day);

                    datePickerDialog.show();
                }
            });
        } else {
            setDueDateTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int year = calendar.get(Calendar.YEAR);
                    int month = calendar.get(Calendar.MONTH);
                    int day = calendar.get(Calendar.DAY_OF_MONTH);

                    DatePickerDialog datePickerDialog = new DatePickerDialog(AddTaskActivity.this,
                            (view, selectedYear, selectedMonth, selectedDay) -> {
                                String dueDate = (selectedMonth + 1) + "-" + selectedDay + "-" + selectedYear;
                                setDueDateTextView.setText(dueDate);
                            }, year, month, day);

                    datePickerDialog.show();
                }
            });

            saveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String updatedTaskText = taskEditText.getText().toString();
                    String updatedDueDate = setDueDateTextView.getText().toString();

                    if (!updatedTaskText.isEmpty() && !updatedDueDate.isEmpty()) {
                        if (intent.hasExtra("task") && intent.hasExtra("due") && intent.hasExtra("id")) {
                            String taskId = intent.getStringExtra("id");
                            updateTaskInFirestore(taskId, updatedTaskText, updatedDueDate);
                        } else {
                            addNewTaskToFirestore(updatedTaskText, updatedDueDate);
                        }
                    } else {
                        Toast.makeText(AddTaskActivity.this, "Task and due date are required", Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }
    }

    private void addNewTaskToFirestore(String taskText, String dueDate) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        Map<String, Object> task = new HashMap<>();
        task.put("task", taskText);
        task.put("due", dueDate);
        task.put("status", 0);

        firestore.collection("task")
                .add(task)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Toast.makeText(AddTaskActivity.this, "Task added to Firestore", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(AddTaskActivity.this, "Failed to save task: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateTaskInFirestore(String taskId, String updatedTaskText, String updatedDueDate) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        DocumentReference taskRef = firestore.collection("task").document(taskId);

        Map<String, Object> updatedData = new HashMap<>();
        updatedData.put("task", updatedTaskText);
        updatedData.put("due", updatedDueDate);

        taskRef.update(updatedData) // Use update to update the existing document
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(AddTaskActivity.this, "Task updated successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(AddTaskActivity.this, "Failed to update task: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

}
