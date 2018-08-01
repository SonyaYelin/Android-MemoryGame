package memoryGame.ui;

import android.content.Context;

public class Card extends android.support.v7.widget.AppCompatImageView {

    private int     frontImage;
    private int     backImage;


    public Card(Context context, int frontImage, int backImage) {
        super(context);
        this.frontImage = frontImage;
        this.backImage = backImage;
    }

    public void turnToFront() {
        this.setImageResource(frontImage);
    }

    public void turnToBack() {
        this.setImageResource(backImage);
    }

    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        if (!(frontImage == ((Card) obj).frontImage))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = frontImage;
        result = 31 * result + backImage;
        return result;
    }

}
