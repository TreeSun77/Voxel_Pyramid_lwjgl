import org.lwjgl.opengl.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.*;

/**
 * Главный класс приложения для отображения воксельной пирамиды.
 */
public class Main {

    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;

    private long window;
    private float angle = 0.0f;

    private boolean[][][] voxels; // Массив вокселей

    // Золотое сечение
    private static final float GOLDEN_RATIO = 1.61803398875f;

    /**
     * Метод запуска приложения.
     */
    public void run() {
        init();
        loop();

        //glfwFreeCallbacks(window);
        glfwDestroyWindow(window);

        glfwTerminate();
    }

    /**
     * Инициализация GLFW и OpenGL.
     */
    private void init() {
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

        window = glfwCreateWindow(WIDTH, HEIGHT, "Voxel Pyramid", NULL, NULL);
        if (window == NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
                glfwSetWindowShouldClose(window, true);
            }
        });

        glfwMakeContextCurrent(window);
        glfwSwapInterval(1);
        glfwShowWindow(window);

        GL.createCapabilities();

        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        glEnable(GL_DEPTH_TEST);
        glEnable(GL_COLOR_MATERIAL);

        // Активация матрицы проекции
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        float aspectRatio = (float) WIDTH / HEIGHT;
        float fov = 45.0f;
        float zNear = 0.1f;
        float zFar = 100.0f;
        float top = (float) Math.tan(Math.toRadians(fov * 0.5)) * zNear;
        float bottom = -top;
        float right = top * aspectRatio;
        float left = -right;
        glFrustum(left, right, bottom, top, zNear, zFar); // Создаем матрицу проекции с помощью glFrustum
        glMatrixMode(GL_MODELVIEW); // Возвращаемся к матрице модели

        // Создаем воксельную пирамиду
        createVoxelPyramid();
    }

    /**
     * Основной игровой цикл.
     */
    private void loop() {
        while (!glfwWindowShouldClose(window)) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            glLoadIdentity();
            glTranslatef(0.0f, 0.0f, -5.0f);
            glRotatef(angle, 0.0f, 1.0f, 0.0f);

            drawVoxelPyramid();

            glfwSwapBuffers(window);
            glfwPollEvents();

            angle += 0.5f;
            if (angle > 360) {
                angle = 0;
            }
        }
    }

    /**
     * Создание воксельной пирамиды.
     */
    private void createVoxelPyramid() {
        // Базовый размер пирамиды
        int baseSize = 5;

        // Высота пирамиды с использованием золотого сечения
        int height = (int) (baseSize * GOLDEN_RATIO);

        // Инициализируем массив вокселей
        voxels = new boolean[baseSize][height][baseSize];

        // Заполняем массив вокселей для создания пирамиды
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < baseSize - y / 2; x++) {
                for (int z = 0; z < baseSize - y / 2; z++) {
                    voxels[x][y][z] = true;
                }
            }
        }
    }

    /**
     * Отрисовка воксельной пирамиды.
     */
    private void drawVoxelPyramid() {
        // Размеры вокселя
        float voxelSize = 0.4f;
        int baseSize = voxels.length;
        int height = voxels[0].length;

        // Отрисовка вокселей
        for (int x = 0; x < baseSize; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < baseSize; z++) {
                    if (voxels[x][y][z]) {
                        float posX = x * voxelSize - (baseSize - 1) * voxelSize / 2;
                        float posY = y * voxelSize - (height - 1) * voxelSize / 2;
                        float posZ = z * voxelSize - (baseSize - 1) * voxelSize / 2;

                        glColor3f((float) x / baseSize, (float) y / height, (float) z / baseSize);
                        drawCube(posX, posY, posZ, voxelSize);
                    }
                }
            }
        }
    }

    /**
     * Отрисовка куба.
     */
    private void drawCube(float x, float y, float z, float size) {
        glBegin(GL_QUADS);
        // Передняя грань
        glVertex3f(x, y, z);
        glVertex3f(x + size, y, z);
        glVertex3f(x + size, y + size, z);
        glVertex3f(x, y + size, z);

        // Задняя грань
        glVertex3f(x, y, z + size);
        glVertex3f(x + size, y, z + size);
        glVertex3f(x + size, y + size, z + size);
        glVertex3f(x, y + size, z + size);

        // Верхняя грань
        glVertex3f(x, y + size, z);
        glVertex3f(x + size, y + size, z);
        glVertex3f(x + size, y + size, z + size);
        glVertex3f(x, y + size, z + size);

        // Нижняя грань
        glVertex3f(x, y, z);
        glVertex3f(x + size, y, z);
        glVertex3f(x + size, y, z + size);
        glVertex3f(x, y, z + size);

        // Левая грань
        glVertex3f(x, y, z);
        glVertex3f(x, y, z + size);
        glVertex3f(x, y + size, z + size);
        glVertex3f(x, y + size, z);

        // Правая грань
        glVertex3f(x + size, y, z);
        glVertex3f(x + size, y, z + size);
        glVertex3f(x + size, y + size, z + size);
        glVertex3f(x + size, y + size, z);
        glEnd();
    }

    /**
     * Точка входа в приложение.
     * @param args Аргументы командной строки.
     */
    public static void main(String[] args) {
        new Main().run();
    }
}
