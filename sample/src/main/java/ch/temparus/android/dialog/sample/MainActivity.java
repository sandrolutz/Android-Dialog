package ch.temparus.android.dialog.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Toast;
import ch.temparus.android.advancedrecyclerview.LinearLayoutManager;
import ch.temparus.android.dialog.Dialog;
import ch.temparus.android.dialog.holder.ListViewHolder;
import ch.temparus.android.dialog.holder.RecyclerViewHolder;
import ch.temparus.android.dialog.holder.ViewHolder;
import ch.temparus.android.dialog.listeners.OnCancelListener;
import ch.temparus.android.dialog.listeners.OnClickListener;
import ch.temparus.android.dialog.listeners.OnItemClickListener;


public class MainActivity extends AppCompatActivity {

    private Dialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        final RadioGroup gravityGroup = (RadioGroup) findViewById(R.id.gravity_group);
        final RadioGroup typeGroup = (RadioGroup) findViewById(R.id.type_group);
        gravityGroup.check(R.id.gravity_bottom);
        typeGroup.check(R.id.type_list);

        Button repeatButton = (Button) findViewById(R.id.button_repeat);
        repeatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mDialog != null) {
                    //getWindow().setStatusBarColor(255);

                    // Create and show the dialog.
                    mDialog.show();
                }
            }
        });

        Button showButton = (Button) findViewById(R.id.button_show);
        showButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Dialog.Builder builder = new Dialog.Builder(MainActivity.this)
                                            .setOnCancelListener(new OnCancelListener() {
                                                @Override
                                                public boolean onCancel(Dialog dialog) {
                                                    Toast.makeText(MainActivity.this, "Cancelled", Toast.LENGTH_SHORT).show();
                                                    return true;
                                                }
                                            })
                                            .setTitle("Test-Title")
                                            .setHeader(R.layout.header)
                                            .setFooter(R.layout.footer);

                if (gravityGroup.getCheckedRadioButtonId() == R.id.gravity_center) {
                    builder.setGravity(Dialog.Gravity.CENTER);
                } else if (gravityGroup.getCheckedRadioButtonId() == R.id.gravity_fullscreen) {
                    builder.setGravity(Dialog.Gravity.FULLSCREEN);
                } else {
                    builder.setGravity(Dialog.Gravity.BOTTOM);
                }

                int typeButtonId = typeGroup.getCheckedRadioButtonId();

                if (typeButtonId == R.id.type_list) {
                    builder.setContentHolder(new ListViewHolder(new SampleListAdapter(MainActivity.this)));
                    builder.setOnItemClickListener(new OnItemClickListener() {
                        @Override
                        public void onItemClick(Dialog dialog, Object item, View view, int position) {
                            Toast.makeText(MainActivity.this, "Item \"" + item.toString() + "\" clicked", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }
                    });
                } else if (typeButtonId == R.id.type_recycler) {
                    SampleRecyclerAdapter adapter = new SampleRecyclerAdapter(MainActivity.this);
                    builder.setContentHolder(new RecyclerViewHolder(new LinearLayoutManager(MainActivity.this, adapter), adapter));
                    builder.setCollapsedHeight(getResources().getDimensionPixelSize(R.dimen.dialog_collapsed_height));
                    builder.setOnItemClickListener(new OnItemClickListener() {
                        @Override
                        public void onItemClick(Dialog dialog, Object item, View view, int position) {
                            Toast.makeText(MainActivity.this, "Item \"" + item.toString() + "\" clicked", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }
                    });
                } else {
                    builder.setContentHolder(new ViewHolder(R.layout.custom_layout));
                    builder.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(Dialog dialog, View view) {
                            if (view.getId() == R.id.custom_layout) {
                                Toast.makeText(MainActivity.this, "clicked", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }
                        }
                    });
                }

                mDialog = builder.create();
                mDialog.show();
            }
        });
    }
}
