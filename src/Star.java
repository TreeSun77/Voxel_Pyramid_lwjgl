import java.util.Random;

import static org.lwjgl.opengl.GL11.*;


// Класс для представления звезды
class Star {
    private float x, y, z; // Координаты
    private float size; // Размер
    private float brightness; // Яркость
    private float red, green, blue; // Цвет
    private static final Random random = new Random(); // Генератор случайных чисел
    private float pulsationRange; // Диапазон пульсации

    // Конструктор
    public Star(float x, float y, float z, float size, float brightness, float red, float green, float blue) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.size = size;
        this.brightness = brightness;
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.pulsationRange = random.nextFloat() * 0.2f + 0.1f; // Генерация диапазона пульсации
    }

    // Генерация случайной звезды
    public static Star generateRandomStar() {
        float x = random.nextFloat() * 20 - 10; // Случайная координата X
        float y = random.nextFloat() * 20 - 10; // Случайная координата Y
        float z = random.nextFloat() * 20 - 10; // Случайная координата Z
        float size = random.nextFloat() * 2 + 1; // Случайный размер
        float brightness = random.nextFloat(); // Случайная яркость
        float red = random.nextFloat(); // Случайный красный цвет
        float green = random.nextFloat(); // Случайный зеленый цвет
        float blue = random.nextFloat(); // Случайный синий цвет
        return new Star(x, y, z, size, brightness, red, green, blue); // Возвращается новый объект звезды
    }

    // Отрисовка звезды
    public void draw() {
        glColor4f(red, green, blue, brightness); // Установка цвета и яркости
        glBegin(GL_POINTS); // Начало определения точки
        glVertex3f(x, y, z); // Установка координаты точки
        glEnd(); // Завершение определения точки
    }

    // Обновление состояния звезды
    public void update() {
        float deltaBrightness = random.nextFloat() * pulsationRange * 2 - pulsationRange; // Изменение яркости
        brightness += deltaBrightness; // Обновление яркости
        brightness = Math.min(1.0f, Math.max(0.0f, brightness)); // Ограничение яркости от 0 до 1

        float deltaSize = random.nextFloat() * pulsationRange * 0.5f - pulsationRange * 0.25f; // Изменение размера
        size += deltaSize; // Обновление размера
        size = Math.min(3.0f, Math.max(1.0f, size)); // Ограничение размера от 1 до 3

        x += random.nextFloat() * 0.01f - 0.005f; // Изменение координаты X
        y += random.nextFloat() * 0.01f - 0.005f; // Изменение координаты Y
        z += random.nextFloat() * 0.01f - 0.005f; // Изменение координаты Z
        if (x < -10) x = 10; // Проверка и коррекция координаты X
        if (x > 10) x = -10; // Проверка и коррекция координаты X
        if (y < -10) y = 10; // Проверка и коррекция координаты Y
        if (y > 10) y = -10; // Проверка и коррекция координаты Y
        if (z < -10) z = 10; // Проверка и коррекция координаты Z
        if (z > 10) z = -10; // Проверка и коррекция координаты Z
    }
}
