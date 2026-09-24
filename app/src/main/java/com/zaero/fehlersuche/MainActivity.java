package com.zaero.fehlersuche;

import android.app.*;
import android.os.*;
import android.content.*;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import java.io.*;
import java.util.*;
import org.json.*;

public class MainActivity extends Activity {
    static final String PREFS="zaero_data";
    static final String KEY_ERRORS="errors";
    static final String MASTER="Chefe";
    final int GOLD=Color.rgb(214,168,79), BG=Color.rgb(18,15,15), PANEL=Color.rgb(31,26,26), TEXT=Color.rgb(245,240,232), MUTED=Color.rgb(190,180,170);
    LinearLayout root, content;
    TextView roleView, sectionView;
    boolean profi=false;
    String section="Anfahren";
    ArrayList<ErrorItem> errors=new ArrayList<>();
    int dp(float v){return (int)(v*getResources().getDisplayMetrics().density+0.5f);}
    TextView tv(String s,float sp){TextView t=new TextView(this);t.setText(s);t.setTextSize(sp);t.setTextColor(TEXT);t.setPadding(dp(12),dp(8),dp(12),dp(8));return t;}
    Button btn(String s){Button b=new Button(this);b.setText(s);b.setTextColor(TEXT);b.setTextSize(15);b.setAllCaps(false);b.setBackground(round(PANEL,dp(10)));return b;}
    GradientDrawable round(int c,int r){GradientDrawable g=new GradientDrawable();g.setColor(c);g.setCornerRadius(r);g.setStroke(dp(1),Color.rgb(85,72,58));return g;}
    @Override public void onCreate(Bundle b){super.onCreate(b);getWindow().setStatusBarColor(Color.rgb(18,15,15));load();build();}
    void build(){
        root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setBackgroundColor(BG);
        LinearLayout top=new LinearLayout(this);top.setGravity(Gravity.CENTER_VERTICAL);top.setPadding(dp(8),dp(6),dp(8),dp(6));
        Button menu=btn("☰"); menu.setTextSize(25); top.addView(menu,new LinearLayout.LayoutParams(dp(55),dp(52)));
        TextView logo=tv("Z.AERO",20);logo.setTextColor(GOLD);logo.setGravity(Gravity.CENTER);top.addView(logo,new LinearLayout.LayoutParams(0,dp(52),1));
        roleView=tv(profi?"PROFI":"BENUTZER",13);roleView.setTextColor(GOLD);roleView.setGravity(Gravity.CENTER);top.addView(roleView,new LinearLayout.LayoutParams(dp(105),dp(52)));
        root.addView(top);
        sectionView=tv(section,15);sectionView.setTextColor(GOLD);sectionView.setGravity(Gravity.CENTER);root.addView(sectionView,new LinearLayout.LayoutParams(-1,dp(38)));
        content=new LinearLayout(this);content.setOrientation(LinearLayout.VERTICAL);content.setPadding(dp(14),dp(8),dp(14),dp(8));ScrollView sv=new ScrollView(this);sv.addView(content);root.addView(sv,new LinearLayout.LayoutParams(-1,0,1));
        Button search=btn("FEHLERSUCHE");search.setTextSize(19);search.setTextColor(Color.WHITE);search.setBackground(round(Color.rgb(103,73,27),dp(12)));root.addView(search,new LinearLayout.LayoutParams(-1,dp(64)));
        setContentView(root);
        menu.setOnClickListener(v->showMenu());
        roleView.setOnClickListener(v->loginDialog());
        search.setOnClickListener(v->showSearch());
        renderList();
    }
    void showMenu(){
        final Dialog d=new Dialog(this); LinearLayout l=dialogBox(); TextView h=tv("Menü",20);h.setTextColor(GOLD);l.addView(h);
        Button a=btn("1. Anfahren"), c=btn("2. Abstellen"); l.addView(a);l.addView(c);
        if(profi){Button add=btn("＋ Fehler anlegen");l.addView(add);add.setOnClickListener(v->{d.dismiss();editError(null);});}
        Button close=btn("Schließen");l.addView(close);close.setOnClickListener(v->d.dismiss());
        a.setOnClickListener(v->{section="Anfahren";sectionView.setText(section);d.dismiss();renderList();});
        c.setOnClickListener(v->{section="Abstellen";sectionView.setText(section);d.dismiss();renderList();});
        d.setContentView(l);d.show();
    }
    LinearLayout dialogBox(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);l.setPadding(dp(18),dp(14),dp(18),dp(14));l.setBackground(round(BG,dp(14)));return l;}
    void loginDialog(){
        if(profi){profi=false;roleView.setText("BENUTZER");renderList();Toast.makeText(this,"Benutzer-Modus",Toast.LENGTH_SHORT).show();return;}
        final Dialog d=new Dialog(this);LinearLayout l=dialogBox();TextView h=tv("Profi-Anmeldung",20);h.setTextColor(GOLD);l.addView(h);
        EditText p=new EditText(this);p.setHint("Passwort");p.setInputType(0x81);p.setTextColor(TEXT);p.setHintTextColor(MUTED);l.addView(p,new LinearLayout.LayoutParams(-1,dp(55)));
        Button ok=btn("Anmelden"),cancel=btn("Abbrechen");l.addView(ok);l.addView(cancel);cancel.setOnClickListener(v->d.dismiss());
        ok.setOnClickListener(v->{if(MASTER.equals(p.getText().toString())){profi=true;roleView.setText("PROFI");d.dismiss();renderList();}else Toast.makeText(this,"Passwort falsch",Toast.LENGTH_SHORT).show();});
        d.setContentView(l);d.show();
    }
    void showSearch(){
        final Dialog d=new Dialog(this);LinearLayout l=dialogBox();TextView h=tv("Fehlersuche",20);h.setTextColor(GOLD);l.addView(h);
        EditText q=new EditText(this);q.setHint("Mindestens 3 Buchstaben");q.setTextColor(TEXT);q.setHintTextColor(MUTED);l.addView(q,new LinearLayout.LayoutParams(-1,dp(55)));
        LinearLayout results=new LinearLayout(this);results.setOrientation(LinearLayout.VERTICAL);l.addView(results);
        Button close=btn("Schließen");l.addView(close);close.setOnClickListener(v->d.dismiss());
        q.addTextChangedListener(new android.text.TextWatcher(){public void beforeTextChanged(CharSequence s,int a,int c,int f){}public void onTextChanged(CharSequence s,int a,int b,int c){results.removeAllViews();String x=s.toString().trim().toLowerCase();if(x.length()<3){results.addView(tv("Bitte mindestens 3 Buchstaben eingeben.",14));return;}for(ErrorItem e:errors)if(e.matches(x)){Button r=btn(e.title+"  •  "+e.area);results.addView(r);r.setOnClickListener(v->{d.dismiss();showError(e);});}}public void afterTextChanged(android.text.Editable e){}});q.requestFocus();d.setContentView(l);d.show();q.postDelayed(()->((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).showSoftInput(q,InputMethodManager.SHOW_IMPLICIT),250);
    }
    void renderList(){if(content==null)return;content.removeAllViews();ArrayList<ErrorItem> list=new ArrayList<>();for(ErrorItem e:errors)if(e.area.equals(section))list.add(e);if(list.isEmpty()){TextView empty=tv("Noch keine Fehler gespeichert.\nAls Profi kannst du hier Fehler und Lösungen anlegen.",16);empty.setGravity(Gravity.CENTER);content.addView(empty,new LinearLayout.LayoutParams(-1,dp(160)));}for(ErrorItem e:list){Button b=btn("FEHLER  "+e.title+"\n"+e.description);b.setGravity(Gravity.START|Gravity.CENTER_VERTICAL);content.addView(b,new LinearLayout.LayoutParams(-1,dp(92)));b.setOnClickListener(v->showError(e));}}
    void showError(ErrorItem e){
        final Dialog d=new Dialog(this);LinearLayout l=dialogBox();TextView h=tv(e.title,21);h.setTextColor(GOLD);l.addView(h);
        addLabel(l,"Bereich: "+e.area);addLabel(l,"Beschreibung: "+e.description);addLabel(l,"Ursache: "+e.cause);addLabel(l,"Lösung: "+e.solution);
        if(!e.images.isEmpty()){addLabel(l,"Bilder:");for(String name:e.images){Button im=btn("🖼 "+name);l.addView(im);}}
        if(profi){Button edit=btn("Bearbeiten"),del=btn("Fehler löschen");l.addView(edit);l.addView(del);edit.setOnClickListener(v->{d.dismiss();editError(e);});del.setOnClickListener(v->{new AlertDialog.Builder(this).setTitle("Fehler löschen?").setMessage(e.title).setNegativeButton("Abbrechen",null).setPositiveButton("Löschen",(x,w)->{deleteImages(e);errors.remove(e);save();d.dismiss();renderList();}).show();});}
        Button close=btn("Schließen");l.addView(close);close.setOnClickListener(v->d.dismiss());d.setContentView(l);d.show();
    }
    void addLabel(LinearLayout l,String s){TextView t=tv(s,15);t.setPadding(dp(8),dp(7),dp(8),dp(7));l.addView(t);}
    void editError(ErrorItem e){
        boolean fresh=e==null;if(fresh)e=new ErrorItem();
        final ErrorItem target=e;final Dialog d=new Dialog(this);LinearLayout l=dialogBox();TextView h=tv(fresh?"Fehler anlegen":"Fehler bearbeiten",20);h.setTextColor(GOLD);l.addView(h);
        EditText title=field("Fehlerbezeichnung",target.title),desc=field("Beschreibung",target.description),cause=field("Ursache",target.cause),sol=field("Lösung",target.solution);l.addView(title);l.addView(desc);l.addView(cause);l.addView(sol);
        Spinner area=new Spinner(this);String[] areas={"Anfahren","Abstellen"};ArrayAdapter<String> ad=new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,areas);area.setAdapter(ad);area.setSelection(target.area.equals("Abstellen")?1:0);l.addView(area);
        Button addImg=btn("＋ Bild hinzufügen");l.addView(addImg);
        LinearLayout imageBox=new LinearLayout(this); imageBox.setOrientation(LinearLayout.VERTICAL); l.addView(imageBox);
        refreshImageButtons(target,imageBox);
        addImg.setOnClickListener(v->pickImage(target,imageBox));
        Button saveB=btn("Speichern"),cancel=btn("Abbrechen");l.addView(saveB);l.addView(cancel);cancel.setOnClickListener(v->d.dismiss());
        saveB.setOnClickListener(v->{String t=title.getText().toString().trim();if(t.length()<1){title.setError("Bitte ausfüllen");return;}target.title=t;target.description=desc.getText().toString().trim();target.cause=cause.getText().toString().trim();target.solution=sol.getText().toString().trim();target.area=areas[area.getSelectedItemPosition()];if(fresh)errors.add(target);save();d.dismiss();renderList();});
        d.setContentView(l);d.show();
    }
    EditText field(String hint,String val){EditText e=new EditText(this);e.setHint(hint);e.setText(val==null?"":val);e.setTextColor(TEXT);e.setHintTextColor(MUTED);e.setPadding(dp(10),dp(5),dp(10),dp(5));e.setBackground(round(PANEL,dp(8)));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(58));p.setMargins(0,dp(4),0,dp(4));e.setLayoutParams(p);return e;}
    void pickImage(ErrorItem e,View imageBox){Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.setType("image/*");i.addCategory(Intent.CATEGORY_OPENABLE);startActivityForResult(i,1001);pending=e;pendingImageBox=imageBox;}
    ErrorItem pending;View pendingImageBox;
    void refreshImageButtons(ErrorItem e,LinearLayout box){box.removeAllViews(); if(e.images.isEmpty()){box.addView(tv("Keine Bilder",14));return;} for(String name:new ArrayList<>(e.images)){Button b=btn("🖼 "+name+"   ✕");box.addView(b);b.setOnClickListener(v->{File f=new File(getFilesDir(),name);if(f.exists())f.delete();e.images.remove(name);save();refreshImageButtons(e,box);});}}
    @Override protected void onActivityResult(int r,int c,Intent data){super.onActivityResult(r,c,data);if(r==1001&&c==RESULT_OK&&data!=null&&data.getData()!=null&&pending!=null){Uri u=data.getData();try{String name="img_"+System.currentTimeMillis()+".jpg";File out=new File(getFilesDir(),name);InputStream in=getContentResolver().openInputStream(u);FileOutputStream fos=new FileOutputStream(out);byte[] buf=new byte[8192];int n;while((n=in.read(buf))>0)fos.write(buf,0,n);in.close();fos.close();pending.images.add(name);if(pendingImageBox instanceof LinearLayout)refreshImageButtons(pending,(LinearLayout)pendingImageBox);save();}catch(Exception ex){Toast.makeText(this,"Bild konnte nicht gespeichert werden",Toast.LENGTH_SHORT).show();}}}
    void deleteImages(ErrorItem e){for(String n:e.images){File f=new File(getFilesDir(),n);if(f.exists())f.delete();}}
    void save(){try{JSONArray a=new JSONArray();for(ErrorItem e:errors)a.put(e.toJson());getSharedPreferences(PREFS,0).edit().putString(KEY_ERRORS,a.toString()).apply();}catch(Exception ignored){}}
    void load(){try{String s=getSharedPreferences(PREFS,0).getString(KEY_ERRORS,"[]");JSONArray a=new JSONArray(s);for(int i=0;i<a.length();i++)errors.add(ErrorItem.from(a.getJSONObject(i)));}catch(Exception ignored){}}
    static class ErrorItem{
        String title="",description="",cause="",solution="",area="Anfahren";ArrayList<String> images=new ArrayList<>();
        boolean matches(String q){String z=(title+" "+description+" "+cause+" "+solution+" "+area).toLowerCase();return z.contains(q);}
        JSONObject toJson()throws Exception{JSONObject o=new JSONObject();o.put("title",title);o.put("description",description);o.put("cause",cause);o.put("solution",solution);o.put("area",area);JSONArray a=new JSONArray();for(String s:images)a.put(s);o.put("images",a);return o;}
        static ErrorItem from(JSONObject o)throws Exception{ErrorItem e=new ErrorItem();e.title=o.optString("title");e.description=o.optString("description");e.cause=o.optString("cause");e.solution=o.optString("solution");e.area=o.optString("area","Anfahren");JSONArray a=o.optJSONArray("images");if(a!=null)for(int i=0;i<a.length();i++)e.images.add(a.getString(i));return e;}
       }
       }
       
