package com.patan.gimnasio.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.Toast;

import com.patan.gimnasio.database.GymnasioDBAdapter;
import com.patan.gimnasio.R;
import com.patan.gimnasio.domain.CustomAdapterRoutine;
import com.patan.gimnasio.domain.ExFromRoutine;
import com.patan.gimnasio.domain.Routine;
import com.patan.gimnasio.services.ApiHandler;

import java.util.ArrayList;
import java.util.Arrays;

// En esta actividad se mostrara una lista de rutinas creadas ademas de la opcion de crear una nueva rutina
//  - Crear rutina (boton flotante) llevara a una RoutineEditActivity vacia
//  - Con una rutina creada podremos:
//      - Verla/Editarla, se abrira una RoutineEditActivity rellenada con los datos de la rutina
//      - Ejecutarla, se abrira una RoutineExecuteActivity para ejecutarla
public class RoutineListActivity extends AppCompatActivity {

    private ListView l;
    private GymnasioDBAdapter db;

    private static final int DELETE_ID = 1;
    private String user_type;
    private String gym_name = "Rutina gratuita";
    private FloatingActionButton fab;
    private FloatingActionButton deleteAll;
//    private Spinner spinner;
    private Button boton;
    private CheckBox check;
//    private EditText busqueda;
    private DeleteRoutineTask task;
    private Menu optionsMenu;

    private EditText sv;
    private Spinner sp;


    public GymnasioDBAdapter getGymnasioDBAdapter(){
        return db;
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routine_list);

        db = new GymnasioDBAdapter(this);
        db.open();
        l = (ListView)findViewById(R.id.dbRoutinesList);

        fab = (FloatingActionButton) findViewById(R.id.floatingActionButton);
//        busqueda = (EditText) (findViewById(R.id.busqueda));
//        spinner = (Spinner) findViewById(R.id.spinner);
        //deleteAll = (FloatingActionButton) findViewById(R.id.deletebutton);
        //check = (CheckBox) findViewById(R.id.checkBox);
        //Mio


        ArrayList<String> categories = new ArrayList<String>();
        categories.add("Nombre");
        categories.add("Objetivo");

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories);
//        spinner.setAdapter(dataAdapter);
//
//        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                busqueda.setText("");
//                fillData();
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//
//            }
//        });
//
//
//        busqueda.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                String text = spinner.getSelectedItem().toString(); //Para saber sobre que categoria se etsa buscando
//                if (s.length() == 0) {
//                    fillData();
//                } else if (text.equals("Nombre")) {
//                    fillDataByName(s.toString());
//                } else if (text.equals("Objetivo")) {
//                    fillDataByObj(s.toString());
//                }
//            }

//            @Override
//            public void afterTextChanged(Editable s) {
//
//            }
//        });

        l.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                Adapter adapter = l.getAdapter();
                //Sin checkbox, guardarse esto por si acaso
                //Cursor item = (Cursor) adapter.getItem(position);
                //int pos = item.getColumnIndex(GymnasioDBAdapter.KEY_RO_ID);
                //long id_rut = item.getLong(pos);
                //Con checkbox
                Routine item = (Routine) adapter.getItem(position);
                Cursor r = null;
                if (user_type.equals("free")) r = db.getFreemiumRoutineByName(item.getName());
                else r = db.getPremiumRoutineByName(item.getName(),gym_name);
                long id_rut = r.getLong(0);
                Intent intent = new Intent(v.getContext(), RoutineEditActivity.class);
                intent.putExtra("MODE","view");
                intent.putExtra("USERTYPE", user_type);
                intent.putExtra("GYMNAME",gym_name);
                intent.putExtra("ID",id_rut);
                startActivity(intent);
            }
        });

        l.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
        l.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                if(l.getCheckedItemCount() <= 0) {
                    fab.setVisibility(View.VISIBLE);
                } else {
                    fab.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                menu.add(0,1,1,"Eliminar");
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                if(item.getItemId() == 1){ // Eliminar
                    delete();
                }
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
            }
        });

        l.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                System.out.println("hola");
                view.setSelected(true);
                l.setItemChecked(position, true);
                return false;
            }
        });

        Intent intent = getIntent();
        user_type = intent.getStringExtra("USERTYPE");

        // Si es modo premium o trainer cogemos ademas el nombre del gym
        if (user_type.equals("premium") || user_type.equals("trainer")) {
            gym_name = intent.getStringExtra("GYMNAME");
        }

        // Rellenamos la lista
        fillData();
    }

    // Menu que se crea unicamente en modo premium o trainer
    public boolean onCreateOptionsMenu(Menu menu) {
        if (user_type.equals("premium") || user_type.equals("trainer")) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.routine_list_menu, menu);
        } else {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.routine_list_menu2, menu);
        }

        LinearLayout rl = (LinearLayout) menu.findItem(R.id.customsearch).getActionView();
        sv = (EditText) rl.findViewById(R.id.etSearch);
        sp = (Spinner) rl.findViewById(R.id.spinnerAb);


        ArrayList<String> categories = new ArrayList<String>();
        categories.add("Nombre");
        categories.add("Objetivo");
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, categories);
        sp.setAdapter(dataAdapter);

        sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sv.setText("");
                fillData();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        sv.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String text = sp.getSelectedItem().toString(); //Para saber sobre que categoria se etsa buscando
                if (s.length() == 0) {
                    fillData();
                } else if (text.equals("Nombre")) {
                    fillDataByName(s.toString());
                } else if (text.equals("Objetivo")) {
                    fillDataByObj(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        if (item.getItemId() == R.id.logout) {
            db.logout();
            // Esto nos hara volver al menu principal directamente limpiando las actividades que tenga por encima (identificate)
            Intent intent = new Intent(this,MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
        return true;
    }

    // Si pulsa el boton de volver atras y esta en modo premium o trainer volvera al menu principal
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        sv.setText("");
        Log.d("BACK PRESSED", sv.getText().toString());
        startActivity(intent);
        super.onBackPressed();

    }


    private void fillData() {
        Cursor routines = null ;
        //fab.setImageResource(R.drawable.ic_shopping_cart_white);
        if (user_type.equals("free")) {
            // Get all of the routines from the database and create the item list
            routines = db.fetchFreemiumRoutines();
            startManagingCursor(routines);
        } else if (user_type.equals("premium")) {
            fab.hide();
            getSupportActionBar().setTitle("Rutinas " + gym_name);
            routines = db.fetchPremiumRoutines(gym_name);
            startManagingCursor(routines);
        } else if (user_type.equals("trainer")) {
            getSupportActionBar().setTitle("Rutinas " + gym_name);
            routines = db.fetchPremiumRoutines(gym_name);
            startManagingCursor(routines);
        }
        Log.d("Tamanio: ", ""+routines.getCount());
        // Create an array to specify the fields we want to display in the list (only NAME)
        String[] from = new String[] {GymnasioDBAdapter.KEY_RO_NAME, GymnasioDBAdapter.KEY_RO_OBJ};
        // and an array of the fields we want to bind those fields to (in this case just text1)
        int[] to = new int[] { R.id.ro_row };
        // Now create an array adapter and set it to display using our row
        SimpleCursorAdapter notes =
                new SimpleCursorAdapter(this, R.layout.routines_row_checkbox, routines, from, to,0);

        ArrayList<Routine> listOfRoutines = new ArrayList<>();
        //Como necesitamos una lista de rutinas, pasamos del cursor a esta lista

        routines.moveToFirst();
        for(int i = 0; i < routines.getCount();i++){
            String idR = routines.getString(routines.getColumnIndex(GymnasioDBAdapter.KEY_RO_IDR));
            Routine nR = new Routine(routines.getString(4),
                    routines.getString(2),
                    routines.getString(1),
                    idR);
            listOfRoutines.add(nR);
            routines.moveToNext();
            Log.d("ARRAY Routines ", nR.getIdR() + " " + nR.getName() + " " + nR.getNameGym() + " " + nR.getObjective());
        }
        l.setAdapter(new CustomAdapterRoutine(this, listOfRoutines));//
        //registerForContextMenu(l);
    }


    private void fillDataByName(String name) {
        Cursor routines = null ;
        if (user_type.equals("free")) {
            // Get all of the routines from the database and create the item list
            routines = db.getFreemiumRoutineByName(name);
            startManagingCursor(routines);
        } else if (user_type.equals("premium") || user_type.equals("trainer")) {
            routines = db.getPremiumRoutineByName(name,gym_name);
            startManagingCursor(routines);
        }
        // Create an array to specify the fields we want to display in the list (only NAME)
        String[] from = new String[] {GymnasioDBAdapter.KEY_RO_NAME};
        // and an array of the fields we want to bind those fields to (in this case just text1)
        int[] to = new int[] { R.id.ro_row };
        // Now create an array adapter and set it to display using our row
        SimpleCursorAdapter notes =
                new SimpleCursorAdapter(this, R.layout.routines_row, routines, from, to,0);
        ArrayList<Routine> listOfRoutines = new ArrayList<>();
        //Como necesitamos una lista de rutinas, pasamos del cursor a esta lista

        routines.moveToFirst();
        for(int i = 0; i < routines.getCount();i++){
            listOfRoutines.add(new Routine(routines.getString(4),routines.getString(2),routines.getString(1)));
            routines.moveToNext();
        }

        l.setAdapter(new CustomAdapterRoutine(this, listOfRoutines));//

        //l.setAdapter(notes);
        //registerForContextMenu(l);
    }

    private void fillDataByObj(String obj) {
        Cursor routines = null ;
        if (user_type.equals("free")) {
            // Get all of the routines from the database and create the item list
            routines = db.getFreemiumRoutineByObj(obj);
            startManagingCursor(routines);
        } else if (user_type.equals("premium") || user_type.equals("trainer")) {
            routines = db.getPremiumRoutineByObj(obj,gym_name);
            startManagingCursor(routines);
        }
        // Create an array to specify the fields we want to display in the list (only NAME)
        String[] from = new String[] {GymnasioDBAdapter.KEY_RO_NAME};
        // and an array of the fields we want to bind those fields to (in this case just text1)
        int[] to = new int[] { R.id.ro_row };
        // Now create an array adapter and set it to display using our row
        SimpleCursorAdapter notes =
                new SimpleCursorAdapter(this, R.layout.routines_row, routines, from, to,0);

        ArrayList<Routine> listOfRoutines = new ArrayList<>();
        //Como necesitamos una lista de rutinas, pasamos del cursor a esta lista

        routines.moveToFirst();
        for(int i = 0; i < routines.getCount();i++){
            listOfRoutines.add(new Routine(routines.getString(4),routines.getString(2),routines.getString(1)));
            routines.moveToNext();
        }

        l.setAdapter(new CustomAdapterRoutine(this, listOfRoutines));//
        //l.setAdapter(notes);
        //registerForContextMenu(l);
    }


    /**
     * Metodo que cambia a la actividad ExerciseViewActivity en modo crear nueva rutina
     */
    public void goToRoutineEditCreate(View v) {
        Intent intent = new Intent(this, RoutineEditActivity.class);
        intent.putExtra("MODE","new");
        intent.putExtra("ID",0);
        intent.putExtra("USERTYPE", user_type);
        intent.putExtra("GYMNAME",gym_name);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        fillData();
    }


    public class DeleteRoutineTask extends AsyncTask<Void, Void, Boolean> {

        private Context mCtx;
        private long id;
        private ApiHandler api;

        DeleteRoutineTask(long id,Context ctx) {
            this.mCtx = ctx;
            this.id = id;
            api = new ApiHandler(mCtx);
        }

        @Override
        protected Boolean doInBackground (Void... params) {

            String idR = db.getPremiumIdr(id);
            Log.d("Premium", "Deleting premium routine on remote server with id " + idR);
            boolean ok = api.deletePremiumRoutine(idR);
            if (ok) {
                return true;
            } return false;
        }
        @Override
        protected void onPostExecute(final Boolean success) {
            CharSequence text;
            if (success) {
                text = "Rutina eliminada en el servidor correctamente";
                db.deleteRoutine(id);
                fillData();
            } else {
                text = "Algo ha ido mal, comprueba tu conexión a internet";
            }
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(mCtx, text, duration);
            toast.setGravity(Gravity.BOTTOM, 0, 100);
            toast.show();
        }
        @Override
        protected void onCancelled() {
            task = null;
        }
    }


    /*Funcion que compruba cuantos checkboxes hay seleccionados, y borra las rutinas asociadas a ellos*/
    public void countCheckMarks() {
        int total = 0 ;
        int mListLength = l.getCount();
        Adapter adapter = l.getAdapter();
        if (user_type.equals("free")) {
            SparseBooleanArray pos = l.getCheckedItemPositions();
            System.out.println("ay: " + pos.toString());
            for (int i = 0; i < pos.size(); i++) {
                //l.setItemChecked(pos.keyAt(i), false);
                if(pos.valueAt(i)){
                    Routine item = (Routine) adapter.getItem(pos.keyAt(i));
                    Cursor c = db.getFreemiumRoutineByName(item.getName());
                    long id = c.getLong(0);
                    db.deleteRoutine(id);
                }
            }
        } else {
            SparseBooleanArray pos = l.getCheckedItemPositions();
            System.out.println("ay: " + pos.toString());
            for (int i = 0; i < pos.size() ; i++) {
                //l.setItemChecked(pos.keyAt(i), false);
                if (pos.valueAt(i)) {

                    Routine item = (Routine) adapter.getItem(pos.keyAt(i));
                    Cursor c = db.getPremiumRoutineByIdR(item.getIdR(), item.getNameGym());
                    Log.d("DELETE ROUTINE", "ID: " + item.getIdR() + " namegym: " + item.getNameGym());
                    long id = c.getLong(c.getColumnIndex(GymnasioDBAdapter.KEY_RO_ID));
                    task=new DeleteRoutineTask(id, this);
                    task.execute((Void) null);
                }
            }
        }

        //Tenemos que mirar si el usuario borró mientras estaba buscando
        if(TextUtils.isEmpty(sv.toString())) fillData();
        else{
            String s = sv.getText().toString();
             if (sp.getSelectedItem().toString().equals("Nombre")) {
                 fillDataByName(s);
             } else if (sp.getSelectedItem().toString().equals("Objetivo")) {
                 fillDataByObj(s);
             }
        }
        if (total == 0) {
            //Toast.makeText(
            //        this, "No hay rutinas seleccionadas para borrar",Toast.LENGTH_LONG).show();

        }
        else{
            //Toast.makeText(
            //        this, "Se han borrado " + total + " rutinas",Toast.LENGTH_LONG).show();
        }
    }

    public void delete(){
        AlertDialog.Builder builder = new AlertDialog.Builder(RoutineListActivity.this);
        builder.setCancelable(true);
        builder.setTitle("Confirmacion");
        builder.setMessage("Desea borrar " + l.getCheckedItemCount() + " rutinas?");
        builder.setPositiveButton("SI",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                            countCheckMarks();
                    }
                });
        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void Prueba(View v) {
        String msg = "WOAS";
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }


}

