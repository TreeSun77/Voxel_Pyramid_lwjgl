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
        glFrustum(left, right, bottom, top, zNear, zFar);
        glMatrixMode(GL_MODELVIEW); // Возвращаемся к матрице модели
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

    private void drawVoxelPyramid() {
        // Размеры пирамиды
        float pyramidWidth = 1.0f;
        float pyramidHeight = 1.0f;
        float pyramidDepth = 1.0f;

        // Количество кубов по ширине, высоте и глубине
        int cubesX = 5;
        int cubesY = 5;
        int cubesZ = 5;

        // Расстояние между кубами
        float spacingX = pyramidWidth / cubesX;
        float spacingY = pyramidHeight / cubesY;
        float spacingZ = pyramidDepth / cubesZ;

        for (int x = 0; x < cubesX; x++) {
            for (int y = 0; y < cubesY; y++) {
                for (int z = 0; z < cubesZ; z++) {
                    float posX = -pyramidWidth / 2 + x * spacingX;
                    float posY = -pyramidHeight / 2 + y * spacingY;
                    float posZ = -pyramidDepth / 2 + z * spacingZ;

                    glColor3f((float) x / cubesX, (float) y / cubesY, (float) z / cubesZ);
                    drawCube(posX, posY, posZ, spacingX, spacingY, spacingZ);
                }
            }
        }
    }

    private void drawCube(float x, float y, float z, float width, float height, float depth) {
        // Верхняя грань
        drawQuad(x, y + height, z, width, depth);
        // Нижняя грань
        drawQuad(x, y, z, width, depth);
        // Передняя грань
        drawQuad(x, y, z + depth, width, height);
        // Задняя грань
        drawQuad(x, y, z, width, height);
        // Левая грань
        drawQuad(x, y, z, depth, height);
        // Правая грань
        drawQuad(x + width, y, z, depth, height);
    }

    private void drawQuad(float x, float y, float z, float width, float height) {
        glBegin(GL_QUADS);
        glVertex3f(x, y, z);
        glVertex3f(x + width, y, z);
        glVertex3f(x + width, y + height, z);
        glVertex3f(x, y + height, z);
        glEnd();
    }

    public static void main(String[] args) {
        new Main().run();
    }
}
