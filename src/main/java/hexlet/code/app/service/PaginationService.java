package hexlet.code.app.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Function;

/**
 * Сервис для обработки пагинированных запросов и формирования стандартизированных ответов.
 * Предоставляет универсальный метод для работы с пагинацией любых типов данных.
 *
 * <p>Сервис автоматически добавляет заголовок <i><b>X-Total-Count</b></i> с общим количеством элементов
 * и возвращает данные в формате {@link ResponseEntity}.</p>
 */
@Service
public class PaginationService {
    /**
     * Создает пагинированный ответ на основе переданных параметров и функции получения данных.
     *
     * @param <T>      тип возвращаемых данных
     * @param end      конечный индекс элементов (параметр _end)
     * @param start    начальный индекс элементов (параметр _start)
     * @param sort     поле для сортировки (параметр _sort)
     * @param order    направление сортировки ("ASC" или "DESC", параметр _order)
     * @param function функция, которая принимает {@link Pageable} и возвращает {@link Page} с данными.
     *                 Обычно передается как ссылка на метод сервиса (например, userService::getPage)
     * @return {@link ResponseEntity} содержащий:
     * <ul>
     *   <li>Список элементов текущей страницы в теле ответа</li>
     *   <li>Заголовок <i><b>X-Total-Count</b></i> с общим количеством элементов</li>
     *   <li>HTTP статус 200 OK</li>
     * </ul>
     * @throws IllegalArgumentException если параметры пагинации некорректны
     * @throws NullPointerException     если function равна null
     * @apiNote Пример использования:
     * <pre>{@code
     * @GetMapping("/users")
     * public ResponseEntity<List<UserDTO>> getUsers(
     *         @RequestParam("_end") int end,
     *         @RequestParam("_start") int start,
     *         @RequestParam("_sort") String sort,
     *         @RequestParam("_order") String order) {
     *     return paginationService.getPaginatedResponse(
     *         end, start, sort, order, userService::getPage);
     * }
     * }</pre>
     */
    public <T> ResponseEntity<List<T>> getPaginatedResponse(
            int end,
            int start,
            String sort,
            String order,
            Function<Pageable, Page<T>> function) {

        int perPage = end - start;
        int pageNumber = (int) Math.ceil((double) start / perPage);
        Sort.Direction sotrDirection = Sort.Direction.valueOf(order);
        Pageable pageable = PageRequest.of(pageNumber, perPage, sotrDirection, sort);
        Page<T> page = function.apply(pageable);

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(page.getTotalElements()));

        return ResponseEntity.ok()
                .headers(headers)
                .body(page.getContent());
    }
}
