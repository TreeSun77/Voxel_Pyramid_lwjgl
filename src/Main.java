import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import java.util.ArrayList;
import java.util.List;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.*;

/**
 * Главный класс приложения для отображения воксельной пирамиды.
 */
public class Main {

    private static final int WINDOW_WIDTH = 800; // Ширина окна
    private static final int WINDOW_HEIGHT = 600; // Высота окна
    private static final double Z_NEAR = 0.1; // Ближняя плоскость отсечения
    private static final double Z_FAR = 100.0; // Дальняя плоскость отсечения
    private static final int SPHERICAL_SLICES = 16; // Количество сегментов вдоль долготы
    private static final int SPHERICAL_STACKS = 16; // Количество сегментов вдоль широты

    private long window; // Идентификатор окна
    private float angle = 0.0f; // Угол вращения

    private List<Coordinate> voxels; // Список координат вокселей

    // Золотое сечение
    private static final double GOLDEN_RATIO = (1 + Math.sqrt(5)) / 2;
    private static final double PI = Math.PI;

    /**
     * Метод запуска приложения.
     */
    public void run() {
        init(); // Инициализация
        loop(); // Основной игровой цикл

        glfwDestroyWindow(window); // Уничтожение окна
        glfwTerminate(); // Завершение работы GLFW
    }

    /**
     * Инициализация GLFW и OpenGL.
     */
    private void init() {
        // Инициализация GLFW
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        // Установка параметров окна GLFW
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // Окно не видимо при создании
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // Окно можно изменять по размеру

        // Создание окна
        window = glfwCreateWindow(WINDOW_WIDTH, WINDOW_HEIGHT, "Voxel Pyramid", NULL, NULL);
        if (window == NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        // Установка обработчика нажатия клавиш
        try (GLFWKeyCallback keyCallback = new GLFWKeyCallback() {
            @Override
            public void invoke(long window, int key, int scancode, int action, int mods) {
                if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
                    glfwSetWindowShouldClose(window, true); // Закрытие окна при нажатии Escape
                }
            }
        }) {
            glfwSetKeyCallback(window, keyCallback);
        }

        // Создание контекста OpenGL и отображение окна
        glfwMakeContextCurrent(window);
        glfwSwapInterval(1); // Включение вертикальной синхронизации
        glfwShowWindow(window);

        // Загрузка OpenGL функций
        GL.createCapabilities();

        // Очистка экрана в черный цвет
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        // Включение буфера глубины и цветовых материалов
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_COLOR_MATERIAL);

        // Установка матрицы проекции
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        double fov = Math.toRadians(90); // Угол обзора в радианах (90 градусов)
        double aspectRatio = (double) WINDOW_WIDTH / WINDOW_HEIGHT;
        double top = Math.tan(fov / 2) * Z_NEAR;
        double bottom = -top;
        double left = aspectRatio * bottom;
        double right = aspectRatio * top;
        glFrustum(left, right, bottom, top, Z_NEAR, Z_FAR); // Установка матрицы проекции

        glMatrixMode(GL_MODELVIEW); // Возвращаемся к матрице модели

        // Создание воксельной пирамиды
        createVoxelPyramid();
    }

    /**
     * Основной игровой цикл.
     */
    private void loop() {
        while (!glfwWindowShouldClose(window)) {
            // Очистка буфера цвета и глубины
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            // Сброс текущей матрицы
            glLoadIdentity();

            // Перемещение вокруг оси Y и вглубь сцены
            glTranslatef(0.0f, 0.0f, -5.0f);

            // Вращение вокруг оси Y
            glRotatef(angle, 0.0f, 1.0f, 0.0f);

            // Отрисовка воксельной пирамиды
            drawVoxelPyramid();

            // Обмен буферов и обработка событий
            glfwSwapBuffers(window);
            glfwPollEvents();

            // Увеличение угла вращения
            angle = (angle + 0.5f) % 360;
        }
    }

    /**
     * Создание воксельной пирамиды.
     */
    private void createVoxelPyramid() {
        // Определение размеров пирамиды
        int baseSize = 5;
        int height = (int) (baseSize * GOLDEN_RATIO);

        // Инициализация списка координат вокселей
        voxels = new ArrayList<>();

        // Рекурсивное добавление координат вокселей
        createVoxel(0, height, baseSize);
    }

    /**
     * Рекурсивное добавление координат вокселей.
     */
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

    /**
     * Отрисовка воксельной пирамиды.
     */
    private void drawVoxelPyramid() {
        // Размер вокселя
        float voxelSize = 0.4f;
        int baseSize = 5;
        int height = (int) (baseSize * GOLDEN_RATIO);

        // Отрисовка вокселей
        for (Coordinate voxel : voxels) {
            float posX = voxel.x * voxelSize - (baseSize - 1) * voxelSize / 2;
            float posY = voxel.y * voxelSize - (height - 1) * voxelSize / 2;
            float posZ = voxel.z * voxelSize - (baseSize - 1) * voxelSize / 2;

            glColor3f((float) voxel.x / baseSize, (float) voxel.y / height, (float) voxel.z / baseSize);
            drawSphere(posX, posY, posZ, voxelSize / 2);
        }
    }

    /**
     * Отрисовка сферы.
     */
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

    // Класс для хранения координат вокселя
    private static class Coordinate {
        int x, y, z;

        public Coordinate(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    public static void main(String[] args) {
        new Main().run();
    }
}
