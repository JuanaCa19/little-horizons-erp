import java.util.NoSuchElementException;
import view.MainMenuView;
import static util.ConsoleUI.printInfo;

public class Main {
    public static void main(String[] args) {
        try {
            new MainMenuView().start();
        } catch (NoSuchElementException e) {
            printInfo("Entrada de consola finalizada. Hasta pronto.");
        }
    }
}
