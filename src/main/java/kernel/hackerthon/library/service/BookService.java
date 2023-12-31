package kernel.hackerthon.library.service;

import jakarta.servlet.http.HttpSession;
import kernel.hackerthon.library.domain.Book;
import kernel.hackerthon.library.domain.User;
import kernel.hackerthon.library.dto.BookRequest;
import kernel.hackerthon.library.dto.GoogleBooksResponse;
import kernel.hackerthon.library.repository.BookRepository;
import kernel.hackerthon.library.repository.RentalRepository;
import kernel.hackerthon.library.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class BookService {

    private final BookRepository bookRepository;
    private final RentalRepository rentalRepository;
    private final UserRepository userRepository;

    public void save(Book book){
        bookRepository.save(book);
    }
    // 현재 서가에 있는 모든 책의 리스트를 조회
    public List<Book> findBooksByIsRecoveryIsFalse(){
        return bookRepository.findBooksByIsRecoveryIsFalse();
    }
    // 현재 대출이 가능한 모든 책의 리스트를 조회
    public List<Book> findBooksAvailable(){
        return bookRepository.findBookByIsRecoveryIsFalseAndIsRentalIsFalse();
    }
    public Optional<Book> getBook(Long bookId){
        return bookRepository.findById(bookId);
    }

    public GoogleBooksResponse searchBookWithIsbn(Map<Object, String> map) {
        String isbn = map.get("isbn");
        String apiKey = map.get("apiKey");

        String apiUrl = "https://www.googleapis.com/books/v1/volumes?q=isbn:" + isbn + "&key=" + apiKey;

        System.out.println(apiUrl);

        RestTemplate restTemplate = new RestTemplate();

        GoogleBooksResponse response = restTemplate.getForObject(apiUrl, GoogleBooksResponse.class);

        return response;
    }


    public void addBook(BookRequest addBookRequest, HttpSession session) {
        User findUser = findByUser((Long) session.getAttribute("loginUser"));
        bookRepository.save(addBookRequest.toEntity(findUser));
    }

    private User findByUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }

    // 책 반납하기
//    @Transactional
//    public int returnBook(Long bookId){
//        Optional<Book> book = bookRepository.findById(bookId);
//        if (book.isPresent()) {
//            Book bookEntity = book.get();
//            Long id = bookEntity.getId();
//            Optional<Rental> rental = rentalRepository.findById(id);
//            if (rental.isPresent()) {
//                bookEntity.returnByBook();
//                Rental rentalEntity = rental.get();
//                rentalEntity.inputReturnDate();
//                // 변경 사항을 저장
//                bookRepository.save(bookEntity);
//                rentalRepository.save(rentalEntity);
//                return 0;    // 정상적으로 처리되면 0 출력
//            } else {
//                return 1;    // Rental엔티티에 문제가 생긴 경우 1 출력
//            }
//        } else {
//            return 2;        // Book엔티티에 문제가 생긴 경우 2 출력
//        }
//    }
    @Transactional
    public void recover(Long bookId, HttpSession session) {
        Book findBook = bookRepository.findById(bookId).orElseThrow(NoSuchElementException::new);
        User findUser = userRepository.findById((Long) session.getAttribute("loginUser"))
                .orElseThrow(NoSuchElementException::new);
        // 현재 찾은 책이 렌탈중이라면 회수하지 못함
        try{
            if(findBook.getIsRental()){
                throw new IllegalAccessException();
            }
        }catch (IllegalAccessException e){
            e.printStackTrace();
        }

        bookRepository.save(new Book(findBook.getId(),
                findBook.getName(), findBook.getIsRental(),
                !findBook.getIsRecovery(), findUser));
    }

    public List<Book> findMyBooks(HttpSession session){
        return bookRepository.findBooksByUserIdAndIsRecoveryIsFalseAndIsRentalIsFalse((Long) session.getAttribute("loginUser"));
    }
}


