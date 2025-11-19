import { ChangeEvent, useState } from 'react';
import { Eye, EyeOff } from 'lucide-react';

type FormFieldProps = {
  id: string;
  label: string;
  type: string;
  value: string;
  onChange: (e: ChangeEvent<HTMLInputElement>) => void;
  placeholder: string;
  disabled: boolean;
};

function FormField({
  id,
  label,
  type,
  value,
  onChange,
  placeholder,
  disabled,
}: FormFieldProps) {
  const [showPassword, setShowPassword] = useState(false);

  const isPasswordField = type === 'password';

  return (
    <div className="relative">
      <label
        htmlFor={id}
        className="block text-xs uppercase tracking-wider mb-3 text-txtcolor-300"
      >
        {label}
      </label>
      <input
        id={id}
        type={isPasswordField ? (showPassword ? 'text' : 'password') : type}
        value={value}
        onChange={onChange}
        placeholder={placeholder}
        disabled={disabled}
        required
        className="w-full max-h-[41px] text-center py-4 px-3 rounded-[22px] bg-inputbg placeholder-txtcolor-300 focus:outline-none focus:ring-2 focus:ring-secondary"
      />
      {isPasswordField && (
        <button
          type="button"
          onClick={() => setShowPassword(!showPassword)}
          className="absolute inset-y-0 right-0 top-7 pr-3 flex items-center text-form-icon)]"
          aria-label={showPassword ? 'Ukryj hasło' : 'Pokaż hasło'}
        >
          {showPassword ? <EyeOff size={20} /> : <Eye size={20} />}
        </button>
      )}
    </div>
  );
}

export default FormField;
