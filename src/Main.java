import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Main {

    private static final int WINDOW_WIDTH = 800; // Ширина окна
    private static final int WINDOW_HEIGHT = 600; // Высота окна
    private static final double Z_NEAR = 0.1; // Ближняя граница видимости
    private static final double Z_FAR = 100.0; // Дальняя граница видимости
    private static final int SPHERICAL_SLICES = 16; // Количество сегментов для сферы
    private static final int SPHERICAL_STACKS = 16; // Количество стеков для сферы

    private long window; // Идентификатор окна
    private float angle = 0.0f; // Угол поворота объекта

    private List<Coordinate> voxels; // Список вокселей
    private List<Star> stars; // Список звезд

    private static final double GOLDEN_RATIO = (1 + Math.sqrt(5)) / 2; // Золотое сечение
    private static final double PI = Math.PI; // Значение числа Пи
    private static final float VOXEL_SIZE = 0.4f; // Размер вокселя
    private static final float DISPLACEMENT = 0.02f; // Величина смещения

    // Метод запуска программы
    public void run() {
        init(); // Инициализация
        loop(); // Основной цикл

        // Закрытие окна и завершение работы GLFW
        glfwDestroyWindow(window);
        glfwTerminate();
    }

    // Метод инициализации
    private void init() {
        // Инициализация GLFW
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        // Установка параметров окна GLFW
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE); // Запрещаем изменение размера окна

        // Создание окна GLFW
        window = glfwCreateWindow(WINDOW_WIDTH, WINDOW_HEIGHT, "Voxel Pyramid", NULL, NULL);
        if (window == NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        // Обработчик нажатия клавиш GLFW
        try (GLFWKeyCallback keyCallback = new GLFWKeyCallback() {
            @Override
            public void invoke(long window, int key, int scancode, int action, int mods) {
                if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
                    glfwSetWindowShouldClose(window, true);
                }
            }
        }) {
            glfwSetKeyCallback(window, keyCallback);
        }

        // Установка текущего контекста OpenGL и параметров
        glfwMakeContextCurrent(window);
        glfwSwapInterval(1);
        glfwShowWindow(window);

        GL.createCapabilities();

        // Установка параметров OpenGL
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_COLOR_MATERIAL);
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        double fov = Math.toRadians(90);
        double aspectRatio = (double) WINDOW_WIDTH / WINDOW_HEIGHT;
        double top = Math.tan(fov / 2) * Z_NEAR;
        double bottom = -top;
        double left = aspectRatio * bottom;
        double right = aspectRatio * top;
        glFrustum(left, right, bottom, top, Z_NEAR, Z_FAR);
        glMatrixMode(GL_MODELVIEW);

        // Создание воксельной пирамиды и списка звезд
        createVoxelPyramid();
        stars = new ArrayList<>();
        generateStars();
    }

    // Основной цикл программы
    private void loop() {
        while (!glfwWindowShouldClose(window)) {
            // Очистка буферов и установка матрицы
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            glLoadIdentity();
            glTranslatef(0.0f, 0.0f, -5.0f);
            glRotatef(angle, 0.0f, 1.0f, 0.0f);

            // Отрисовка воксельной пирамиды и звезд
            drawVoxelPyramid();
            drawStars();

            // Обмен буферов и обработка событий GLFW
            glfwSwapBuffers(window);
            glfwPollEvents();

            // Обновление угла поворота и звезд
            angle = (angle + 0.5f) % 360;
            updateStars();
        }
    }

    // Метод создания воксельной пирамиды
    private void createVoxelPyramid() {
        int baseSize = 5;
        int height = (int) (baseSize * GOLDEN_RATIO);
        voxels = new ArrayList<>();
        createVoxel(0, height, baseSize);
    }

    // Рекурсивный метод создания вокселя
    private void createVoxel(int y, int maxHeight, int baseSize) {
        if (y >= maxHeight) {
            return;
        }

        for (int x = 0; x < baseSize - y / 2; x++) {
            for (int z = 0; z < baseSize - y / 2; z++) {
                voxels.add(new Coordinate(x, y, z));
            }
        }

        createVoxel(y + 1, maxHeight, baseSize);
    }

    // Метод отрисовки воксельной пирамиды
    private void drawVoxelPyramid() {
        int baseSize = 5;
        int height = (int) (baseSize * GOLDEN_RATIO);

        for (Coordinate voxel : voxels) {
            float posX = voxel.x * VOXEL_SIZE - (baseSize - 1) * VOXEL_SIZE / 2;
            float posY = voxel.y * VOXEL_SIZE - (height - 1) * VOXEL_SIZE / 2;
            float posZ = voxel.z * VOXEL_SIZE - (baseSize - 1) * VOXEL_SIZE / 2;
            glColor3f((float) voxel.x / baseSize, (float) voxel.y / height, (float) voxel.z / baseSize);
            drawSphere(posX, posY, posZ, VOXEL_SIZE / 2);
        }
    }

    // Метод отрисовки сферы
    private void drawSphere(float x, float y, float z, float radius) {
        for (int i = 0; i < SPHERICAL_SLICES; i++) {
            double theta0 = 2 * PI * ((double) i / SPHERICAL_SLICES);
            double theta1 = 2 * PI * ((double) (i + 1) / SPHERICAL_SLICES);

            glBegin(GL_QUAD_STRIP);
            for (int j = 0; j <= SPHERICAL_STACKS; j++) {
                double phi = PI * (-0.5 + (double) j / SPHERICAL_STACKS);
                double cosPhi = Math.cos(phi);
                double sinPhi = Math.sin(phi);
                double x0 = x + radius * Math.cos(theta0) * cosPhi;
                double y0 = y + radius * Math.sin(theta0) * cosPhi;
                double z0 = z + radius * sinPhi;
                double x1 = x + radius * Math.cos(theta1) * cosPhi;
                double y1 = y + radius * Math.sin(theta1) * cosPhi;
                double z1 = z + radius * sinPhi;
                glNormal3d(x0 - x, y0 - y, z0 - z);
                glVertex3d(x0, y0, z0);
                glNormal3d(x1 - x, y1 - y, z1 - z);
                glVertex3d(x1, y1, z1);
            }
            glEnd();
        }
    }

    // Метод генерации списка звезд
    private void generateStars() {
        int numStars = 100;
        for (int i = 0; i < numStars; i++) {
            stars.add(Star.generateRandomStar());
        }
    }

    // Метод отрисовки звезд
    private void drawStars() {
        for (Star star : stars) {
            float x = star.getX();
            float y = star.getY();
            float z = star.getZ();
            float brightness = star.getBrightness();

            float red = star.getRed() + DISPLACEMENT * x;
            float green = star.getGreen() + DISPLACEMENT * y;
            float blue = star.getBlue() + DISPLACEMENT * z;

            red = Math.max(0.0f, Math.min(1.0f, red));
            green = Math.max(0.0f, Math.min(1.0f, green));
            blue = Math.max(0.0f, Math.min(1.0f, blue));

            glColor4f(red, green, blue, brightness);
            glBegin(GL_POINTS);
            glVertex3f(x, y, z);
            glEnd();
        }
    }

    // Метод обновления состояния звезд
    private void updateStars() {
        for (Star star : stars) {
            star.update();
        }
    }

    // Внутренний класс для представления координаты
    private static class Coordinate {
        int x, y, z;

        public Coordinate(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    // Главный метод для запуска программы
    public static void main(String[] args) {
        new Main().run();
    }
}

// Внутренний класс для представления звезды
class Star {
    private float x, y, z;
    private float brightness;
    private float red, green, blue;
    private static final Random random = new Random();

    // Конструктор для создания звезды с заданными параметрами
    public Star(float x, float y, float z, float brightness, float red, float green, float blue) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.brightness = brightness;
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    // Метод для генерации случайной звезды
    public static Star generateRandomStar() {
        float x = random.nextFloat() * 20 - 10;
        float y = random.nextFloat() * 20 - 10;
        float z = random.nextFloat() * 20 - 10;
        float brightness = random.nextFloat();
        float red = random.nextFloat();
        float green = random.nextFloat();
        float blue = random.nextFloat();
        return new Star(x, y, z, brightness, red, green, blue);
    }

    // Метод для обновления параметров звезды
    public void update() {
        float deltaBrightness = random.nextFloat() * 0.05f - 0.025f;
        brightness += deltaBrightness;
        if (brightness < 0.1f) brightness = 0.1f;
        if (brightness > 1.0f) brightness = 1.0f;
        x += random.nextFloat() * 0.01f - 0.005f;
        y += random.nextFloat() * 0.01f - 0.005f;
        z += random.nextFloat() * 0.01f - 0.005f;
        if (x < -10) x = 10;
        if (x > 10) x = -10;
        if (y < -10) y = 10;
        if (y > 10) y = -10;
        if (z < -10) z = 10;
        if (z > 10) z = -10;
    }

    // Геттеры для получения координат и параметров звезды
    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getZ() {
        return z;
    }

    public float getBrightness() {
        return brightness;
    }

    public float getRed() {
        return red;
    }

    public float getGreen() {
        return green;
    }

    public float getBlue() {
        return blue;
    }
}
