import org.lwjgl.glfw.*; // Импорт классов для работы с GLFW
import org.lwjgl.opengl.*; // Импорт классов для работы с OpenGL

import java.util.ArrayList; // Импорт класса ArrayList из пакета java.util
import java.util.List; // Импорт интерфейса List из пакета java.util

import static org.lwjgl.glfw.GLFW.*; // Статический импорт методов из класса GLFW из библиотеки LWJGL
import static org.lwjgl.opengl.GL11.*; // Статический импорт методов из класса GL11 из библиотеки LWJGL
import static org.lwjgl.system.MemoryUtil.*; // Статический импорт методов из класса MemoryUtil из библиотеки LWJGL

public class Main {
    private static final int WINDOW_WIDTH = 800; // Ширина окна
    private static final int WINDOW_HEIGHT = 600; // Высота окна
    private static final double Z_NEAR = 0.1; // Ближняя плоскость отсечения
    private static final double Z_FAR = 100.0; // Дальняя плоскость отсечения
    private static final int SPHERICAL_SLICES = 16; // Количество срезов для отрисовки сферы
    private static final int SPHERICAL_STACKS = 16; // Количество стеков для отрисовки сферы

    private long window; // Идентификатор окна
    private float angle = 0.0f; // Угол поворота

    private List<Coordinate> voxels; // Список координат вокселей
    private List<Star> stars; // Список звезд

    private static final double GOLDEN_RATIO = (1 + Math.sqrt(5)) / 2; // Золотое сечение
    private static final double PI = Math.PI; // Значение числа Пи
    private static final float VOXEL_SIZE = 0.4f; // Размер вокселя

    // Метод запуска приложения
    public void run() {
        init(); // Инициализация
        loop(); // Основной цикл приложения

        glfwDestroyWindow(window); // Уничтожение окна GLFW
        glfwTerminate(); // Завершение работы с GLFW
    }

    // Инициализация приложения
    private void init() {
        if (!glfwInit()) { // Если не удалось инициализировать GLFW
            throw new IllegalStateException("Unable to initialize GLFW"); // Выбрасывается исключение
        }

        glfwDefaultWindowHints(); // Установка параметров окна по умолчанию
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // Установка параметра видимости окна
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // Установка параметра изменяемости размера окна
        glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE); // Установка параметра изменяемости размера окна

        window = glfwCreateWindow(WINDOW_WIDTH, WINDOW_HEIGHT, "Voxel Pyramid", NULL, NULL); // Создание окна GLFW
        if (window == NULL) { // Если окно не удалось создать
            throw new RuntimeException("Failed to create the GLFW window"); // Выбрасывается исключение
        }

        try (GLFWKeyCallback keyCallback = new GLFWKeyCallback() { // Обработчик клавиатуры для окна GLFW
            @Override
            public void invoke(long window, int key, int scancode, int action, int mods) {
                if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) { // Если нажата клавиша ESCAPE и была отпущена
                    glfwSetWindowShouldClose(window, true); // Установка флага закрытия окна
                }
            }
        }) {
            glfwSetKeyCallback(window, keyCallback); // Установка обработчика клавиатуры для окна GLFW
        }

        glfwMakeContextCurrent(window); // Устанавливается контекст OpenGL в текущее окно GLFW
        glfwSwapInterval(1); // Установка интервала обновления буфера кадров
        glfwShowWindow(window); // Отображение окна

        GL.createCapabilities(); // Создание возможностей OpenGL

        glClearColor(0.0f, 0.0f, 0.0f, 0.0f); // Установка цвета очистки экрана
        glEnable(GL_DEPTH_TEST); // Включение буфера глубины
        glEnable(GL_COLOR_MATERIAL); // Включение материала для цветов

        glMatrixMode(GL_PROJECTION); // Выбор матрицы проекции
        glLoadIdentity(); // Загрузка единичной матрицы проекции
        double fov = Math.toRadians(90); // Преобразование угла в радианы
        double aspectRatio = (double) WINDOW_WIDTH / WINDOW_HEIGHT; // Вычисление соотношения сторон окна
        double top = Math.tan(fov / 2) * Z_NEAR; // Вычисление верхней плоскости отсечения
        double bottom = -top; // Вычисление нижней плоскости отсечения
        double left = aspectRatio * bottom; // Вычисление левой плоскости отсечения
        double right = aspectRatio * top; // Вычисление правой плоскости отсечения
        glFrustum(left, right, bottom, top, Z_NEAR, Z_FAR); // Установка перспективной матрицы

        glMatrixMode(GL_MODELVIEW); // Выбор матрицы моделирования

        createVoxelPyramid(); // Создание воксельной пирамиды
        stars = new ArrayList<>(); // Инициализация списка звезд
        generateStars(); // Генерация звезд
    }

    // Основной цикл приложения
    private void loop() {
        while (!glfwWindowShouldClose(window)) { // Пока окно не должно быть закрыто
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // Очистка буфера цвета и буфера глубины
            glLoadIdentity(); // Загрузка единичной матрицы моделирования
            glTranslatef(0.0f, 0.0f, -5.0f); // Перемещение объектов по оси Z
            glRotatef(angle, 0.0f, 1.0f, 0.0f); // Поворот объектов вокруг оси Y

            drawVoxelPyramid(); // Отрисовка воксельной пирамиды
            drawStars(); // Отрисовка звезд

            glfwSwapBuffers(window); // Переключение буферов
            glfwPollEvents(); // Обработка событий

            angle = (angle + 0.5f) % 360; // Увеличение угла поворота, предотвращающее переполнение
            updateStars(); // Обновление состояния звезд
        }
    }

    // Создание воксельной пирамиды
    private void createVoxelPyramid() {
        int baseSize = 7; // Размер основания пирамиды
        int height = (int) (baseSize * GOLDEN_RATIO); // Высота пирамиды
        voxels = new ArrayList<>(); // Инициализация списка координат вокселей
        createVoxel(0, height, baseSize); // Создание воксельной пирамиды
    }

    // Рекурсивное создание воксельной пирамиды
    private void createVoxel(int y, int maxHeight, int baseSize) {
        if (y >= maxHeight) { // Если достигнута максимальная высота
            return; // Завершение рекурсии
        }

        for (int x = 0; x < baseSize - y / 2; x++) { // Перебор координат по X
            for (int z = 0; z < baseSize - y / 2; z++) { // Перебор координат по Z
                voxels.add(new Coordinate(x, y, z)); // Добавление координаты в список вокселей
            }
        }

        createVoxel(y + 1, maxHeight, baseSize); // Рекурсивный вызов для следующего слоя пирамиды
    }

    // Отрисовка воксельной пирамиды
    private void drawVoxelPyramid() {
        int baseSize = 7; // Размер основания пирамиды
        int height = (int) (baseSize * GOLDEN_RATIO); // Высота пирамиды

        for (Coordinate voxel : voxels) { // Перебор координат вокселей
            float posX = voxel.x * VOXEL_SIZE - (baseSize - 1) * VOXEL_SIZE / 2; // Вычисление позиции по X
            float posY = voxel.y * VOXEL_SIZE - (height - 1) * VOXEL_SIZE / 2; // Вычисление позиции по Y
            float posZ = voxel.z * VOXEL_SIZE - (baseSize - 1) * VOXEL_SIZE / 2; // Вычисление позиции по Z
            glColor3f((float) voxel.x / baseSize, (float) voxel.y / height, (float) voxel.z / baseSize); // Установка цвета вокселя
            drawSphere(posX, posY, posZ, VOXEL_SIZE / 1.8f); // Отрисовка сферы вокселя
        }
    }

    // Отрисовка сферы
    private void drawSphere(float x, float y, float z, float radius) {
        for (int i = 0; i < SPHERICAL_SLICES; i++) { // Перебор срезов сферы
            double theta0 = 2 * PI * ((double) i / SPHERICAL_SLICES); // Вычисление угла theta начального сегмента
            double theta1 = 2 * PI * ((double) (i + 1) / SPHERICAL_SLICES); // Вычисление угла theta конечного сегмента

            glBegin(GL_QUAD_STRIP); // Начало определения полосы квадратов
            for (int j = 0; j <= SPHERICAL_STACKS; j++) { // Перебор стеков сферы
                double phi = PI * (-0.5 + (double) j / SPHERICAL_STACKS); // Вычисление угла phi
                double cosPhi = Math.cos(phi); // Косинус угла phi
                double sinPhi = Math.sin(phi); // Синус угла phi
                double x0 = x + radius * Math.cos(theta0) * cosPhi; // Координата X начальной точки
                double y0 = y + radius * Math.sin(theta0) * cosPhi; // Координата Y начальной точки
                double z0 = z + radius * sinPhi; // Координата Z начальной точки
                double x1 = x + radius * Math.cos(theta1) * cosPhi; // Координата X конечной точки
                double y1 = y + radius * Math.sin(theta1) * cosPhi; // Координата Y конечной точки
                double z1 = z + radius * sinPhi; // Координата Z конечной точки
                glNormal3d(x0 - x, y0 - y, z0 - z); // Установка нормали для точки начального сегмента
                glVertex3d(x0, y0, z0); // Установка координаты начальной точки
                glNormal3d(x1 - x, y1 - y, z1 - z); // Установка нормали для точки конечного сегмента
                glVertex3d(x1, y1, z1); // Установка координаты конечной точки
            }
            glEnd(); // Завершение определения полосы квадратов
        }
    }

    // Генерация звезд
    private void generateStars() {
        int numStars = 888; // Количество звезд
        for (int i = 0; i < numStars; i++) { // Перебор звезд
            stars.add(Star.generateRandomStar()); // Добавление случайно сгенерированной звезды
        }
    }

    // Отрисовка звезд
    private void drawStars() {
        for (Star star : stars) { // Перебор звезд
            star.draw(); // Отрисовка звезды
        }
    }

    // Обновление состояния звезд
    private void updateStars() {
        for (Star star : stars) { // Перебор звезд
            star.update(); // Обновление звезды
        }
    }

    // Внутренний класс для представления координаты
    private static class Coordinate {
        int x, y, z; // Координаты

        // Конструктор
        public Coordinate(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    // Главный метод приложения
    public static void main(String[] args) {
        new Main().run(); // Создание экземпляра класса Main и запуск приложения
    }
}


