package com.patan.gimnasio.activities;

import android.content.Intent;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ListView;

import com.patan.gimnasio.database.GymnasioDBAdapter;
import com.patan.gimnasio.R;
import com.patan.gimnasio.domain.Exercise;

import java.util.ArrayList;

// En esta actividad se mostrara una lista con todos los ejercicios, ademas de una barra de busqueda para poder filtrar
public class ExerciseListActivity extends AppCompatActivity {

    private ListView l;
    private GymnasioDBAdapter db;
    private static final int ADD_ID = 1;
    private String mode_in;

    public GymnasioDBAdapter getGymnasioDbAdapter() {
        return db;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercises_list);
        db = new GymnasioDBAdapter(this);
        db.open();

        l = (ListView)findViewById(R.id.dbExercisesList);

        l.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                Adapter adapter = l.getAdapter();
                Cursor item = (Cursor) adapter.getItem(position);
                int pos = item.getColumnIndex(GymnasioDBAdapter.KEY_RO_ID);
                long id_ex = item.getLong(pos);
                Intent intent = new Intent(v.getContext(), ExerciseViewActivity.class);
                intent.putExtra("ID",id_ex);
                startActivity(intent);
            }
        });

        fillData();
        registerForContextMenu(l);
        Intent intent = getIntent();
        mode_in = intent.getStringExtra("MODE");
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if (mode_in.equals("routine")) {
            super.onCreateContextMenu(menu,v,menuInfo);
            menu.add(Menu.NONE, ADD_ID, Menu.NONE, R.string.menu_add);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getItemId() == ADD_ID) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
            Adapter adapter = l.getAdapter();
            Cursor c = (Cursor) adapter.getItem(info.position);
            int pos = c.getColumnIndex(GymnasioDBAdapter.KEY_EX_ID);
            long id = c.getLong(pos);
            int name = c.getColumnIndex(GymnasioDBAdapter.KEY_EX_NAME);
            String nombre = c.getString(name);

            Intent intent = new Intent(this, AddExerciseToRoutineActivity.class);
            intent.putExtra("MODE","add");
            intent.putExtra("ID",id);
            Log.d("ID SELECTED",""+id);
            intent.putExtra("NAME", nombre);
            startActivityForResult(intent,1);

            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                long id = data.getLongExtra("ID",0);  // Cogemos el ID del ejercicio añadido;
                int series = data.getIntExtra("SERIES",0); // Cogemos las series del ejercicio añadido
                int rep = data.getIntExtra("REP",0); // Cogemos las repeticiones del ejercicio añadido
                double relax = data.getDoubleExtra("RELAX",0); // Cogemos el tiempo de relax del ejercicio añadido

                Intent i = new Intent();
                i.putExtra("ID",id);
                i.putExtra("SERIES",series);
                i.putExtra("REP",rep);
                i.putExtra("RELAX",relax);
                setResult(RESULT_OK,i);
                finish();       // Forzamos volver a la actividad anterior
            }
        }
    }

    private void fillData() {
        // Get all of the exercises from the database and create the item list
        Cursor exercises = db.fetchExercises();
        startManagingCursor(exercises);
        // Create an array to specify the fields we want to display in the list (only NAME)
        String[] from = new String[] {GymnasioDBAdapter.KEY_EX_NAME,GymnasioDBAdapter.KEY_EX_TAG};
        // and an array of the fields we want to bind those fields to (in this case just text1)
        int[] to = new int[] { R.id.ex_row,R.id.ex_row2};
        // Now create an array adapter and set it to display using our row
        SimpleCursorAdapter notes =
                new SimpleCursorAdapter(this, R.layout.exercises_row, exercises, from, to,0);
        l.setAdapter(notes);
    }

    private Exercise test() {
        ArrayList<String> tags = new ArrayList<>();
        tags.add("hola");tags.add("holi");
        Exercise e = new Exercise("test","gemelo","yo k se","algo",tags);
        return e;
    }
}
