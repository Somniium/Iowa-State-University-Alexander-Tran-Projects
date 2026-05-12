package onetoone;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class BookController {

    private final OpenLibraryService openLibraryService;
    private final BookRepository bookRepository;

    public BookController(OpenLibraryService openLibraryService, BookRepository bookRepository) {
        this.openLibraryService = openLibraryService;
        this.bookRepository = bookRepository;
    }

    // Search Open Library (NOT saved yet)
    // Example: /search-book?q=harry%20potter
    @GetMapping("/search-book")
    public List<Book> searchBooks(
            @RequestParam(name = "q") String query,
            @RequestParam(name = "max", defaultValue = "5") int max
    ) {
        return openLibraryService.searchBooks(query, max);
    }

    // Get one Open Library work/edition (NOT saved yet)
    // Example: /book-volume?volumeId=/works/OL82563W
    @GetMapping("/book-volume")
    public Book getVolume(@RequestParam(name = "volumeId") String volumeId) {
        return openLibraryService.getBookByVolumeId(volumeId);
    }

    // Save a book into DB (table: books) using Open Library key as "volumeId"
    @PostMapping("/books/saveByVolumeId")
    public Book saveByVolumeId(@RequestParam(name = "volumeId") String volumeId) {
        return bookRepository.findByVolumeId(volumeId).orElseGet(() -> {
            // Pull details; note authors may be better from search results,
            // but this fetch can provide description + covers.
            Book fetched = openLibraryService.getBookByVolumeId(volumeId);
            return bookRepository.save(fetched);
        });
    }

    @GetMapping("/books")
    public List<Book> getSavedBooks() {
        return bookRepository.findAll();
    }

    @DeleteMapping("/books/{id}")
    public String deleteBook(@PathVariable Long id) {
        if (!bookRepository.existsById(id)) return "Book not found";
        bookRepository.deleteById(id);
        return "Deleted book id=" + id;
    }
}