import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Main {

    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;

    private long window;
    private float angle = 0.0f;

    private boolean[][][] voxels; // Массив вокселей

    public void run() {
        init();
        loop();

        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);

        glfwTerminate();
    }

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

    private void createVoxelPyramid() {
        // Размеры пирамиды
        int pyramidSize = 5;

        // Инициализируем массив вокселей
        voxels = new boolean[pyramidSize][pyramidSize][pyramidSize];

        // Заполняем массив вокселей для создания пирамиды
        for (int y = 0; y < pyramidSize; y++) {
            for (int x = 0; x <= y; x++) {
                for (int z = 0; z <= y; z++) {
                    voxels[x][y][z] = true;
                    voxels[y][x][z] = true;
                    voxels[x][y][y] = true;
                    voxels[y][x][y] = true;
                }
            }
        }
    }

    private void drawVoxelPyramid() {
        // Размеры вокселя
        float voxelSize = 0.5f;

        // Отрисовка вокселей
        for (int x = 0; x < voxels.length; x++) {
            for (int y = 0; y < voxels[x].length; y++) {
                for (int z = 0; z < voxels[x][y].length; z++) {
                    if (voxels[x][y][z]) {
                        float posX = x * voxelSize - (voxels.length - 1) * voxelSize / 2;
                        float posY = y * voxelSize - (voxels[x].length - 1) * voxelSize / 2;
                        float posZ = z * voxelSize - (voxels[x][y].length - 1) * voxelSize / 2;

                        glColor3f((float) x / voxels.length, (float) y / voxels[x].length, (float) z / voxels[x][y].length);
                        drawCube(posX, posY, posZ, voxelSize);
                    }
                }
            }
        }
    }

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

    public static void main(String[] args) {
        new Main().run();
    }
}
