import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
    private List<Star> stars; // Список звезд

    // Золотое сечение
    private static final double GOLDEN_RATIO = (1 + Math.sqrt(5)) / 2;
    private static final double PI = Math.PI;

    private Random random;

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

        // Инициализация списка звезд
        stars = new ArrayList<>();
        random = new Random();

        // Генерация звезд
        generateStars();
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

            // Отрисовка звезд
            drawStars();

            // Обмен буферов и обработка событий
            glfwSwapBuffers(window);
            glfwPollEvents();

            // Увеличение угла вращения
            angle = (angle + 0.5f) % 360;

            // Обновление состояния звезд
            updateStars();
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
        // Проверка, достигли ли мы максимальной высоты пирамиды
        if (y >= maxHeight) {
            return; // Если достигли, выходим из метода
        }

        // Цикл создания вокселей по оси X
        for (int x = 0; x < baseSize - y / 2; x++) {
            // Цикл создания вокселей по оси Z
            for (int z = 0; z < baseSize - y / 2; z++) {
                // Добавление нового вокселя в список вокселей с координатами (x, y, z)
                voxels.add(new Coordinate(x, y, z));
            }
        }

        // Рекурсивный вызов метода для создания вокселей на следующем уровне пирамиды (y + 1)
        createVoxel(y + 1, maxHeight, baseSize);
    }

    /**
     * Отрисовка воксельной пирамиды.
     */
    private void drawVoxelPyramid() {
        // Задание размера вокселя
        float voxelSize = 0.4f;
        int baseSize = 5; // Размер основания пирамиды
        int height = (int) (baseSize * GOLDEN_RATIO); // Высота пирамиды

        // Отрисовка каждого вокселя в списке
        for (Coordinate voxel : voxels) {
            // Вычисление позиции вокселя в пространстве
            float posX = voxel.x * voxelSize - (baseSize - 1) * voxelSize / 2;
            float posY = voxel.y * voxelSize - (height - 1) * voxelSize / 2;
            float posZ = voxel.z * voxelSize - (baseSize - 1) * voxelSize / 2;

            // Вычисление цвета вокселя в зависимости от его позиции
            // Цвет изменяется в зависимости от координаты каждой оси
            // Каждая ось (X, Y, Z) влияет на соответствующий канал RGB
            glColor3f((float) voxel.x / baseSize, (float) voxel.y / height, (float) voxel.z / baseSize);

            // Вызов метода для отрисовки сферы вокруг вокселя с учетом его позиции и размера
            drawSphere(posX, posY, posZ, voxelSize / 2);
        }
    }

    /**
     * Отрисовка сферы.
     */
    private void drawSphere(float x, float y, float z, float radius) {
        // Цикл для разбиения сферы на сегменты вдоль долготы
        for (int i = 0; i < SPHERICAL_SLICES; i++) {
            // Вычисление углов начала и конца текущего сегмента долготы
            double theta0 = 2 * PI * ((double) i / SPHERICAL_SLICES);
            double theta1 = 2 * PI * ((double) (i + 1) / SPHERICAL_SLICES);

            glBegin(GL_QUAD_STRIP); // Начало определения параллельной полосы
            // Цикл для разбиения сегмента долготы на сегменты широты
            for (int j = 0; j <= SPHERICAL_STACKS; j++) {
                // Вычисление угла текущей широты
                double phi = PI * (-0.5 + (double) j / SPHERICAL_STACKS);
                // Вычисление косинуса и синуса текущей широты
                double cosPhi = Math.cos(phi);
                double sinPhi = Math.sin(phi);

                // Вычисление координат текущей вершины
                double x0 = x + radius * Math.cos(theta0) * cosPhi;
                double y0 = y + radius * Math.sin(theta0) * cosPhi;
                double z0 = z + radius * sinPhi;

                double x1 = x + radius * Math.cos(theta1) * cosPhi;
                double y1 = y + radius * Math.sin(theta1) * cosPhi;
                double z1 = z + radius * sinPhi;

                // Нормализация вектора для получения нормали к поверхности сферы
                glNormal3d(x0 - x, y0 - y, z0 - z);
                // Определение вершины
                glVertex3d(x0, y0, z0);
                glNormal3d(x1 - x, y1 - y, z1 - z);
                glVertex3d(x1, y1, z1);
            }
            glEnd(); // Завершение определения параллельной полосы
        }
    }

    /**
     * Генерация звезд на заднем фоне.
     */
    private void generateStars() {
        int numStars = 100; // Количество звезд
        for (int i = 0; i < numStars; i++) {
            float x = random.nextFloat() * 20 - 10; // Случайная координата X в пределах [-10, 10]
            float y = random.nextFloat() * 20 - 10; // Случайная координата Y в пределах [-10, 10]
            float z = random.nextFloat() * 20 - 10; // Случайная координата Z в пределах [-10, 10]
            float brightness = random.nextFloat(); // Случайная яркость звезды
            float red = random.nextFloat(); // Случайный красный канал
            float green = random.nextFloat(); // Случайный зеленый канал
            float blue = random.nextFloat(); // Случайный синий канал
            stars.add(new Star(x, y, z, brightness, red, green, blue));
        }
    }

    /**
     * Отрисовка звезд на заднем фоне.
     */
    private void drawStars() {
        for (Star star : stars) {
            glColor4f(star.red, star.green, star.blue, star.brightness);
            glBegin(GL_POINTS);
            glVertex3f(star.x, star.y, star.z);
            glEnd();
        }
    }

    /**
     * Обновление состояния звезд.
     */
    private void updateStars() {
        for (Star star : stars) {
            // Мерцание звезды (изменение яркости)
            float deltaBrightness = random.nextFloat() * 0.05f - 0.025f;
            star.brightness += deltaBrightness;
            if (star.brightness < 0.1f) star.brightness = 0.1f;
            if (star.brightness > 1.0f) star.brightness = 1.0f;

            // Перемещение звезды (зацикливание при достижении границ)
            star.x += random.nextFloat() * 0.01f - 0.005f;
            star.y += random.nextFloat() * 0.01f - 0.005f;
            star.z += random.nextFloat() * 0.01f - 0.005f;
            if (star.x < -10) star.x = 10;
            if (star.x > 10) star.x = -10;
            if (star.y < -10) star.y = 10;
            if (star.y > 10) star.y = -10;
            if (star.z < -10) star.z = 10;
            if (star.z > 10) star.z = -10;
        }
    }

    // Класс, представляющий координаты вокселя в трехмерном пространстве
    private static class Coordinate {
        int x, y, z; // Координаты вокселя по осям X, Y и Z

        // Конструктор класса, инициализирующий координаты вокселя
        public Coordinate(int x, int y, int z) {
            this.x = x; // Установка координаты X
            this.y = y; // Установка координаты Y
            this.z = z; // Установка координаты Z
        }
    }

    // Класс, представляющий звезду
    private static class Star {
        float x, y, z; // Координаты звезды
        float brightness; // Яркость звезды
        float red, green, blue; // Цвет звезды

        // Конструктор класса, инициализирующий звезду
        public Star(float x, float y, float z, float brightness, float red, float green, float blue) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.brightness = brightness;
            this.red = red;
            this.green = green;
            this.blue = blue;
        }
    }

    // Основной метод программы, который запускает приложение
    public static void main(String[] args) {
        new Main().run(); // Создание нового экземпляра класса Main и вызов метода run()
    }
}
