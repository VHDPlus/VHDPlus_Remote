/*
    Name: SwipeCallback
    Rev: 1.0
    Creator: Leon Beier
    Date: 22.11.2021
    Copyright (c) 2021 Protop Solutions UG. All right reserved.

    Permission is hereby granted, free of charge, to any person obtaining a copy of
    this java code and associated documentation files (the "Java Code"), to deal in the
    Java Code without restriction, including without limitation the rights to use,
    copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the
    Java Code, and to permit persons to whom the Java Code is furnished to do so,
    subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all
    copies or substantial portions of the Java Code.

    THE Java Code IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
    FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
    COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN
    AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
    WITH THE Java Code OR THE USE OR OTHER DEALINGS IN THE Java Code.

    Description:
    This class draws a colored square and an icon next to the element that is swiped to one side
*/

//Thanks Zachery Osborn for explaining how to do this
//https://medium.com/@zackcosborn/step-by-step-recyclerview-swipe-to-delete-and-undo-7bbae1fce27e

package de.protop_solutions.vhdplus.vhdplus_remote.RecyclerViewCallbacks;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import de.protop_solutions.vhdplus.vhdplus_remote.R;

abstract public class SwipeCallback extends ItemTouchHelper.Callback {

    Context context;
    //Color to clear drawing
    private Paint transparentColor;
    //Color to show when swiped
    private int backgroundColor;
    //Drawable to show when swiped in background color
    private ColorDrawable backgroundDrawable;
    //Icon to show when swiped
    private Drawable deleteDrawable;
    private int intrinsicWidth;
    private int intrinsicHeight;
    private int type;
    BroadcastReceiver swipeReceiver;
    private boolean swipe;

    /**
     * Initialize UI elements and set type of callback
     * Adds broadcast receiver for swipe enable
     * @param context
     * @param type 0 = delete (red + trashcan), 1 = edit (green + pencil)
     */
    public SwipeCallback(Context context, int type) {
        this.context = context;
        this.type = type;
        backgroundDrawable = new ColorDrawable();
        if (type == 0) backgroundColor = ContextCompat.getColor(context, R.color.red);
        else backgroundColor = ContextCompat.getColor(context, R.color.green);
        transparentColor = new Paint();
        transparentColor.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        if (type == 0) deleteDrawable = ContextCompat.getDrawable(this.context, R.drawable.ic_delete);
        else deleteDrawable = ContextCompat.getDrawable(this.context, R.drawable.ic_edit);
        intrinsicWidth = deleteDrawable.getIntrinsicWidth();
        intrinsicHeight = deleteDrawable.getIntrinsicHeight();

        swipe = true;

        swipeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                swipe = intent.getBooleanExtra("enable", true);
            }
        };

        LocalBroadcastManager.getInstance(context).registerReceiver(swipeReceiver, new IntentFilter("swipe"));
    }

    /**
     * Unregister broadcast receiver
     */
    public void destroy(){
        LocalBroadcastManager.getInstance(context).unregisterReceiver(swipeReceiver);
    }

    /**
     * Define direction for swipe
     * Disabled if slider touched
     * @param recyclerView
     * @param viewHolder
     * @return
     */
    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        if (swipe) {
            if (type == 0) return makeMovementFlags(0, ItemTouchHelper.LEFT);
            else return makeMovementFlags(0, ItemTouchHelper.RIGHT);
        }
        else return makeMovementFlags(0, 0);
    }

    /**
     * Do nothing when element moved
     * @param recyclerView
     * @param viewHolder
     * @param viewHolder1
     * @return
     */
    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder viewHolder1) {
        return false;
    }

    /**
     * Draws colored rectangle and icon when swiped
     * @param c
     * @param recyclerView
     * @param viewHolder
     * @param dX
     * @param dY
     * @param actionState
     * @param isCurrentlyActive
     */
    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

        View itemView = viewHolder.itemView;
        int itemHeight = itemView.getHeight();

        boolean isCancelled = dX == 0 && !isCurrentlyActive;

        if (isCancelled) {
            if (type == 0)
                clearCanvas(c, itemView.getRight() + dX, (float) itemView.getTop(), (float) itemView.getRight(), (float) itemView.getBottom());
            else
                clearCanvas(c, (float) itemView.getLeft(), (float) itemView.getTop(), (float) itemView.getLeft() + dX, (float) itemView.getBottom());
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            return;
        }

        backgroundDrawable.setColor(backgroundColor);
        if (type == 0) backgroundDrawable.setBounds(itemView.getRight() + (int) dX, itemView.getTop(), itemView.getRight(), itemView.getBottom());
        else backgroundDrawable.setBounds(itemView.getLeft(), itemView.getTop(), itemView.getLeft() + (int) dX, itemView.getBottom());
        backgroundDrawable.draw(c);

        int deleteIconTop = itemView.getTop() + (itemHeight - intrinsicHeight) / 2;
        int deleteIconMargin = ((int) context.getResources().getDimension(R.dimen.element_height) - intrinsicHeight) / 2;
        int deleteIconLeft;
        int deleteIconRight;
        if (type == 0) {
            deleteIconLeft = itemView.getRight() - deleteIconMargin - intrinsicWidth;
            deleteIconRight = itemView.getRight() - deleteIconMargin;
        }else{
            deleteIconLeft = itemView.getLeft() + deleteIconMargin;
            deleteIconRight = itemView.getLeft() + deleteIconMargin + intrinsicWidth;
        }
        int deleteIconBottom = deleteIconTop + intrinsicHeight;


        deleteDrawable.setBounds(deleteIconLeft, deleteIconTop, deleteIconRight, deleteIconBottom);
        deleteDrawable.draw(c);

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }

    /**
     * Draw clear rectangle when swipe canceled
     * @param c
     * @param left
     * @param top
     * @param right
     * @param bottom
     */
    private void clearCanvas(Canvas c, Float left, Float top, Float right, Float bottom) {
        c.drawRect(left, top, right, bottom, transparentColor);
    }

    /**
     * Calls onSwiped when swiped more than 70% of the width
     * @param viewHolder
     * @return
     */
    @Override
    public float getSwipeThreshold(@NonNull RecyclerView.ViewHolder viewHolder) {
        return 0.7f;
    }
}
