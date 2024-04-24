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

        window = glfwCreateWindow(WIDTH, HEIGHT, "Rainbow Rotating Pyramid", NULL, NULL);
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

            drawPyramid();

            glfwSwapBuffers(window);
            glfwPollEvents();

            angle += 0.5f;
            if (angle > 360) {
                angle = 0;
            }
        }
    }

    private void drawPyramid() {
        glBegin(GL_TRIANGLES);

        // Основание пирамиды (квадрат)
        glColor3f(1.0f, 0.0f, 0.0f); // Красный
        glVertex3f(-0.5f, 0.0f, -0.5f);  // Левая нижняя вершина
        glColor3f(0.0f, 1.0f, 0.0f); // Зеленый
        glVertex3f(0.5f, 0.0f, -0.5f);   // Правая нижняя вершина
        glColor3f(0.0f, 0.0f, 1.0f); // Синий
        glVertex3f(0.5f, 0.0f, 0.5f);    // Правая верхняя вершина

        glColor3f(1.0f, 0.0f, 0.0f); // Красный
        glVertex3f(-0.5f, 0.0f, -0.5f);  // Левая нижняя вершина
        glColor3f(0.0f, 0.0f, 1.0f); // Синий
        glVertex3f(0.5f, 0.0f, 0.5f);    // Правая верхняя вершина
        glColor3f(1.0f, 1.0f, 0.0f); // Желтый
        glVertex3f(-0.5f, 0.0f, 0.5f);   // Левая верхняя вершина

        // Боковые грани пирамиды
        glColor3f(1.0f, 0.0f, 1.0f); // Фиолетовый
        glVertex3f(-0.5f, 0.0f, -0.5f);  // Левая нижняя вершина
        glColor3f(0.0f, 1.0f, 1.0f); // Бирюзовый
        glVertex3f(0.0f, 1.0f, 0.0f);    // Вершина пирамиды
        glColor3f(0.0f, 1.0f, 0.0f); // Зеленый
        glVertex3f(0.5f, 0.0f, -0.5f);   // Правая нижняя вершина

        glColor3f(1.0f, 0.0f, 1.0f); // Фиолетовый
        glVertex3f(0.5f, 0.0f, -0.5f);   // Правая нижняя вершина
        glColor3f(0.0f, 1.0f, 1.0f); // Бирюзовый
        glVertex3f(0.0f, 1.0f, 0.0f);    // Вершина пирамиды
        glColor3f(0.0f, 0.0f, 1.0f); // Синий
        glVertex3f(0.5f, 0.0f, 0.5f);    // Правая верхняя вершина

        glColor3f(0.0f, 1.0f, 0.0f); // Зеленый
        glVertex3f(0.5f, 0.0f, 0.5f);    // Правая верхняя вершина
        glColor3f(1.0f, 1.0f, 0.0f); // Желтый
        glVertex3f(0.0f, 1.0f, 0.0f);    // Вершина пирамиды
        glColor3f(1.0f, 0.0f, 0.0f); // Красный
        glVertex3f(-0.5f, 0.0f, 0.5f);   // Левая верхняя вершина

        glColor3f(0.0f, 1.0f, 0.0f); // Зеленый
        glVertex3f(-0.5f, 0.0f, 0.5f);   // Левая верхняя вершина
        glColor3f(1.0f, 0.0f, 1.0f); // Фиолетовый
        glVertex3f(-0.5f, 0.0f, -0.5f);  // Левая нижняя вершина
        glColor3f(0.0f, 1.0f, 1.0f); // Бирюзовый
        glVertex3f(0.0f, 1.0f, 0.0f);    // Вершина пирамиды

        glEnd();
    }

    public static void main(String[] args) {
        new Main().run();
    }
}
