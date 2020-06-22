package uk.co.darkerwaters.scorepal.activities.handlers;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;

import uk.co.darkerwaters.scorepal.activities.fragments.CardHolderMatch;

public class SwipeMatchHandler extends ItemTouchHelper.SimpleCallback {

    private final Drawable leftIcon;
    private final Drawable rightIcon;
    private final ColorDrawable leftBackground, rightBackground;

    public interface SwipeMatchInterface {
        int getSwipeMode();
        int getRightIconResId();
        int getLeftIconResId();
        int getRightColor();
        int getLeftColor();
        void handleSwipeRight(CardHolderMatch viewHolder);
        void handleSwipeLeft(CardHolderMatch viewHolder);
    }

    private final Context context;
    private final SwipeMatchInterface swipeMatchInterface;

    public SwipeMatchHandler(Context context, SwipeMatchInterface swipeMatchInterface) {
        super(0, swipeMatchInterface.getSwipeMode());

        this.context = context;
        this.swipeMatchInterface = swipeMatchInterface;

        rightIcon = ContextCompat.getDrawable(context, this.swipeMatchInterface.getRightIconResId());
        rightBackground = new ColorDrawable(this.swipeMatchInterface.getRightColor());
        rightBackground.setAlpha(80);

        leftIcon = ContextCompat.getDrawable(context, this.swipeMatchInterface.getLeftIconResId());
        leftBackground = new ColorDrawable(this.swipeMatchInterface.getLeftColor());
        leftBackground.setAlpha(80);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder viewHolder1) {
        return false;
    }

    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        View itemView = viewHolder.itemView;
        int backgroundCornerOffset = 20;

        int iconHeight = rightIcon.getIntrinsicHeight() * 2;

        int iconMargin = iconHeight / 4;
        int iconTop = itemView.getTop() + (itemView.getHeight() - iconHeight) / 2;
        int iconBottom = iconTop + iconHeight;

        if (dX > 0) {
            // Swiping to the right
            int iconLeft = itemView.getLeft() + iconMargin;
            int iconRight = itemView.getLeft() + iconMargin + iconHeight;
            rightIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);

            rightBackground.setBounds(itemView.getLeft(), itemView.getTop(),
                    itemView.getLeft() + ((int) dX) + backgroundCornerOffset,
                    itemView.getBottom());
            rightBackground.draw(c);
            rightIcon.draw(c);
        } else if (dX < 0) {
            // Swiping to the left
            int iconLeft = itemView.getRight() - iconMargin - iconHeight;
            int iconRight = itemView.getRight() - iconMargin;
            leftIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);

            leftBackground.setBounds(itemView.getRight() + ((int) dX) - backgroundCornerOffset,
                    itemView.getTop(), itemView.getRight(), itemView.getBottom());
            leftBackground.draw(c);
            leftIcon.draw(c);
        } else {
            // view is unSwiped
            leftBackground.setBounds(0, 0, 0, 0);
            rightBackground.setBounds(0, 0, 0, 0);
        }

    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        CardHolderMatch card = null;
        if (viewHolder instanceof CardHolderMatch) {
            card = (CardHolderMatch)viewHolder;
        }
        if (null != card) {
            switch (i) {
                case ItemTouchHelper.RIGHT:
                    this.swipeMatchInterface.handleSwipeRight(card);
                    break;
                case ItemTouchHelper.LEFT:
                    this.swipeMatchInterface.handleSwipeLeft(card);
                    break;
            }
        }
    }
}
