package com.example.finalmobile.Visible;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import com.example.finalmobile.AboutActivity;
import com.example.finalmobile.GalleryActivity;
import com.example.finalmobile.MainActivity;
public class Utilities {
    private static FrameLayout addPhoto, addLabel;
    private static Context context;
    private static String visibilityState="default";

    public static void init(Context context,FrameLayout addPhoto, FrameLayout addLabel){
        Utilities.context=context;
        Utilities.addPhoto=addPhoto;
        Utilities.addLabel=addLabel;
    }

    public static void handleNavSelected(MenuItem menuItem, String loggedIn){
        int itemId=menuItem.getItemId();
        if(itemId==getResourceId("nav_item1","id")){
            setVisibility(View.VISIBLE,View.INVISIBLE);
        }else if (itemId==getResourceId("nav_item2","id")){
            setVisibility(View.INVISIBLE,View.VISIBLE);
        }else if (itemId==getResourceId("nav_item3","id")){
            Intent intent1=new Intent(context, GalleryActivity.class);
            intent1.putExtra("userEmail",loggedIn);
            context.startActivity(intent1);
        }else if (itemId==getResourceId("nav_item4","id")){
            Intent intent2=new Intent(context,AboutActivity.class);
            intent2.putExtra("userEmail",loggedIn);
            context.startActivity(intent2);
        }else if(itemId==getResourceId("nav_item5","id")){
            Intent intent3=new Intent(context,MainActivity.class);
            intent3.putExtra("userEmail",loggedIn);
            context.startActivity(intent3);

            setVisibility(View.INVISIBLE,View.INVISIBLE);
        }
    }
    private static int getResourceId(String name, String type){
        Resources resources=context.getResources();
        return  resources.getIdentifier(name,type,context.getPackageName());
    }

    private static void setVisibility(int addLabelVisibility, int addPhotoVisibility){
        if(addLabel!=null){
            addLabel.setVisibility(addLabelVisibility);
        }
        if(addPhoto!=null){
            addPhoto.setVisibility(addPhotoVisibility);
        }
    }
}
