package com.example.android.notepad;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class Search extends Activity {
    private EditText searchInput;
    private Button confirmButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_search);

        searchInput = findViewById(R.id.search_input);
        confirmButton = findViewById(R.id.search_button);

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String query = searchInput.getText().toString().trim();
                Intent resultIntent = new Intent();
                resultIntent.putExtra("query", query);  // 将查询内容传递回去
                setResult(RESULT_OK, resultIntent);  // 返回到 NotesList
                finish();  // 关闭当前 Activity
            }
        });
    }
}


